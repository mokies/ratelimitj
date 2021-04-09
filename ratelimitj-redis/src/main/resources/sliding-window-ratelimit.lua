 -- Credit: http://www.dr-josiah.com/2014/11/introduction-to-rate-limiting-with_26.html

local limits = cjson.decode(ARGV[1])
local now = tonumber(ARGV[2])
local weight = tonumber(ARGV[3] or '1')
local strictly_greater = tonumber(ARGV[4] or '1') == 1
local longest_duration = limits[1][1] or 0
local longest_applied_backoff = 0
local saved_keys = {}
local ge_limit = '0'

-- handle cleanup and limit checks
for i, limit in ipairs(limits) do
    local duration = limit[1]
    longest_duration = math.max(longest_duration, duration)
    local precision = limit[3] or duration
    local backoff = limit[4] or 0
    precision = math.min(precision, duration)
    local blocks = math.ceil(duration / precision)
    local saved = {}
    table.insert(saved_keys, saved)
    saved.block_id = math.floor(now / precision)
    saved.trim_before = saved.block_id - blocks + 1
    saved.count_key = duration .. ':' .. precision .. ':'
    saved.ts_key = saved.count_key .. 'o'
    saved.backoff_key = saved.count_key .. 'bo'
    saved.backoff_ts_key = saved.backoff_key .. ':ts'
    saved.backoff = backoff

    for j, key in ipairs(KEYS) do
        local old_ts = redis.call('HGET', key, saved.ts_key)
        old_ts = old_ts and tonumber(old_ts) or saved.trim_before
        if old_ts > now then
            -- don't write in the past
            return '1'
        end
        -- discover what needs to be cleaned up
        local decr = 0
        local dele = {}
        local trim = math.min(saved.trim_before, old_ts + blocks)
        for old_block = old_ts, trim - 1 do
            local bkey = saved.count_key .. old_block
            local bcount = redis.call('HGET', key, bkey)
            if bcount then
                decr = decr + tonumber(bcount)
                table.insert(dele, bkey)
            end
        end

        if (saved.backoff > 0) then -- only if backoff is enabled for this rule
            local applied_backoff = redis.call('HGET', key, saved.backoff_key)

            if (applied_backoff == '1') then -- backoff is in place
                local applied_backoff_ts = redis.call('HGET', key, saved.backoff_ts_key) or 0

                if (now - tonumber(applied_backoff_ts) > saved.backoff) then -- backoff expired
                    redis.call('HDEL', key, unpack({ saved.backoff_key, saved.backoff_ts_key }))
                else
                    return '1' -- backoff still in place, don't record request
                end
            end
        end

        -- handle cleanup
        local cur
        if #dele > 0 then
            redis.call('HDEL', key, unpack(dele))
            -- Guard against "-0" => "ERR value is not an integer or out of range"
            if decr ~= 0 then
                cur = redis.call('HINCRBY', key, saved.count_key, -decr)
            end
        else
            cur = redis.call('HGET', key, saved.count_key)
        end
        -- check our limits
        local count = tonumber(cur or '0') + weight

        if (saved.backoff > 0 and (count == limit[2])) then -- at limit, apply backoff for further calls
            redis.call('HSET', key, saved.backoff_key, '1')
            redis.call('HSET', key, saved.backoff_ts_key, now)

            longest_applied_backoff = math.max(longest_applied_backoff, saved.backoff)
        end

        if count > limit[2] then
            return '1' -- over limit, don't record request
        elseif count == limit[2] and not strictly_greater then
            ge_limit = '1' -- at limit, do record request
        end
    end
end

-- there is enough resources, update the counts IFF needed
if weight ~= 0 then
    for i, limit in ipairs(limits) do
        local saved = saved_keys[i]
        for j, key in ipairs(KEYS) do
            -- update the current timestamp, count, and bucket count
            redis.call('HSET', key, saved.ts_key, saved.trim_before)
            redis.call('HINCRBY', key, saved.count_key, weight)
            redis.call('HINCRBY', key, saved.count_key .. saved.block_id, weight)
        end
    end
end

-- We calculated the longest-duration and longest-applied-backoff limit so we can EXPIRE
-- the whole HASH for quick and easy idle-time cleanup :)
local longest_expire = math.max(longest_applied_backoff, longest_duration)
if longest_expire > 0 then
    for _, key in ipairs(KEYS) do
        redis.call('EXPIRE', key, longest_expire)
    end
end
return ge_limit