package es.moki.rjchocker;


public class Choker {

    public boolean overLimitSliderWindow(String key) {
        return overLimitSliderWindow(key, 1);
    }

    public boolean overLimitSliderWindow(String key, int weight) {
        return false;
    }



//
//    def over_limit_sliding_window(conn, weight=1, limits=[(1, 10), (60, 120), (3600, 240, 60)], redis_time=False):
//            if not hasattr(conn, 'over_limit_sliding_window_lua'):
//    conn.over_limit_sliding_window_lua = conn.register_script(over_limit_sliding_window_lua_)
//
//    now = conn.time()[0] if redis_time else time.time()
//            return conn.over_limit_sliding_window_lua(
//    keys=get_identifiers(), args=[json.dumps(limits), now, weight])
}
