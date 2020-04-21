package tw.nekomimi.nekogram.sub;

import androidx.annotation.NonNull;

import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.telegram.messenger.SharedConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.ErrorListener;

import cn.hutool.core.util.StrUtil;
import tw.nekomimi.nekogram.utils.HttpUtil;
import tw.nekomimi.nekogram.utils.ProxyUtil;

@Index("_id")
@SuppressWarnings("unchecked")
public class SubInfo implements Mappable {

    @Id
    public long _id = -1;
    public String name;
    public List<String> urls = new LinkedList<>();
    public List<String> proxies = new LinkedList<>();
    public long lastFetch = -1L;
    public boolean enable = true;
    public boolean internal;

    public String displayName() {

        if (name.length() < 5) return name;

        return name.substring(0,5) + "...";

    }

    public List<SharedConfig.ProxyInfo> reloadProxies() throws AllTriesFailed {

        HashMap<String,Exception> exceptions = new HashMap<>();

        for (String url : urls) {

            try {

                String source = HttpUtil.get(url);

                return ProxyUtil.parseProxies(source);

            } catch (Exception e) {

                exceptions.put(url,e);

            }

        }

        throw new AllTriesFailed(exceptions);

    }

    public static class AllTriesFailed extends IOException {

        public AllTriesFailed(HashMap<String,Exception> exceptions) {
            this.exceptions = exceptions;
        }

        public HashMap<String,Exception> exceptions;

        @NonNull @Override public String toString() {

            StringBuilder errors = new StringBuilder();

            for (Map.Entry<String, Exception> e : exceptions.entrySet()) {

                errors.append(e.getKey()).append(": ");

                if (StrUtil.isBlank(e.getValue().getMessage())) {

                    errors.append(e.getValue().getMessage());

                } else {

                    errors.append(e.getValue().getClass().getSimpleName());

                }

                errors.append("\n\n");

            }

            return errors.toString();

        }

    }


    @Override
    public Document write(NitriteMapper mapper) {

        if (_id == -1L) _id = NitriteId.newId().getIdValue();

        Document document = new Document();

        document.put("_id", _id);
        document.put("name", name);
        document.put("urls", urls);
        document.put("proxies",proxies);

        document.put("lastFetch", lastFetch);
        document.put("enable", enable);
        document.put("internal", internal);

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {

        _id = document.get("_id", Long.class);
        name = document.get("name", String.class);
        urls = (List<String>) document.get("urls");
        proxies = (List<String>) document.get("proxies");

        lastFetch = document.get("lastFetch",Long.class);
        enable = document.get("enable",Boolean.class);
        internal = document.get("internal",Boolean.class);

    }

}
