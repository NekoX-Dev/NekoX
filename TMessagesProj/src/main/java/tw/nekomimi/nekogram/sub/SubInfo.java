package tw.nekomimi.nekogram.sub;

import com.fasterxml.jackson.databind.JsonNode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SubInfo {

    public String name;

    public List<Mirror> mirrors;

    public static class Mirror {

        public String url;

        public String method;

        public Map<String, String> headers;

    }

    public List<String> proxies;

}
