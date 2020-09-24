/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubboadmin.governance.sync.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubboadmin.registry.common.domain.Consumer;
import com.alibaba.dubboadmin.registry.common.domain.Override;
import com.alibaba.dubboadmin.registry.common.domain.Provider;
import com.alibaba.dubboadmin.registry.common.domain.Route;

public class SyncUtils {

    public static final String SERVICE_FILTER_KEY = ".service";

    public static final String ADDRESS_FILTER_KEY = ".address";

    public static final String ID_FILTER_KEY = ".id";

    /**
     * 将URL解析成 Provider 模型
     *
     * @param pair
     * @return
     */
    public static Provider url2Provider(Pair<Long, URL> pair) {
        if (pair == null) {
            return null;
        }

        Long id = pair.getKey();
        URL url = pair.getValue();

        if (url == null) {
            return null;
        }

        Provider p = new Provider();
        p.setId(id);
        p.setService(url.getServiceKey());
        p.setAddress(url.getAddress());
        p.setApplication(url.getParameter(Constants.APPLICATION_KEY));
        p.setUrl(url.toIdentityString());
        p.setParameters(url.toParameterString());

        p.setDynamic(url.getParameter("dynamic", true));
        p.setEnabled(url.getParameter(Constants.ENABLED_KEY, true));
        p.setWeight(url.getParameter(Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT));
        p.setUsername(url.getParameter("owner"));

        return p;
    }

    /**
     * URL集合转成 Provider 集合，其中 Long型的自增值作为 Provider对象的id
     *
     * @param ps
     * @return
     */
    public static List<Provider> url2ProviderList(Map<Long, URL> ps) {
        List<Provider> ret = new ArrayList<>();
        for (Map.Entry<Long, URL> entry : ps.entrySet()) {
            ret.add(url2Provider(new Pair<>(entry.getKey(), entry.getValue())));
        }
        return ret;
    }

    /**
     * 将URL解析成 Consumer 模型
     *
     * @param pair
     * @return
     */
    public static Consumer url2Consumer(Pair<Long, URL> pair) {
        if (pair == null) {
            return null;
        }

        Long id = pair.getKey();
        URL url = pair.getValue();

        if (null == url) {
            return null;
        }

        Consumer c = new Consumer();
        c.setId(id);
        c.setService(url.getServiceKey());
        c.setAddress(url.getHost());
        c.setApplication(url.getParameter(Constants.APPLICATION_KEY));
        c.setParameters(url.toParameterString());

        return c;
    }

    /**
     * URL集合转成 Consumer 集合，其中 Long型的自增值作为 Consumer 对象的id
     *
     * @param cs
     * @return
     */
    public static List<Consumer> url2ConsumerList(Map<Long, URL> cs) {
        List<Consumer> list = new ArrayList<Consumer>();
        if (cs == null) {
            return list;
        }
        for (Map.Entry<Long, URL> entry : cs.entrySet()) {
            list.add(url2Consumer(new Pair<>(entry.getKey(), entry.getValue())));
        }
        return list;
    }

    /**
     * 将URL解析成 Route 模型
     *
     * @param pair
     * @return
     */
    public static Route url2Route(Pair<Long, URL> pair) {
        if (pair == null) {
            return null;
        }

        Long id = pair.getKey();
        URL url = pair.getValue();

        if (null == url) {
            return null;
        }

        Route r = new Route();
        r.setId(id);
        r.setName(url.getParameter("name"));
        r.setService(url.getServiceKey());
        // 优先级默认为0
        r.setPriority(url.getParameter(Constants.PRIORITY_KEY, 0));
        // 默认启用
        r.setEnabled(url.getParameter(Constants.ENABLED_KEY, true));
        // 当路由结果为空时，是否强制执行，默认不强制执行。如果不强制执行，路由结果为空的路由规则将自动失效。
        r.setForce(url.getParameter(Constants.FORCE_KEY, false));
        r.setRule(url.getParameterAndDecoded(Constants.RULE_KEY));
        return r;
    }

    /**
     * URL集合转成 Route 集合，其中 Long型的自增值作为 Route 对象的id
     *
     * @param cs
     * @return
     */
    public static List<Route> url2RouteList(Map<Long, URL> cs) {
        List<Route> list = new ArrayList<>();
        if (cs == null) {
            return list;
        }
        for (Map.Entry<Long, URL> entry : cs.entrySet()) {
            list.add(url2Route(new Pair<>(entry.getKey(), entry.getValue())));
        }
        return list;
    }

    /**
     * 将URL解析成 Override 模型
     *
     * @param pair
     * @return
     */
    public static Override url2Override(Pair<Long, URL> pair) {
        if (pair == null) {
            return null;
        }

        Long id = pair.getKey();
        URL url = pair.getValue();

        if (null == url) {
            return null;
        }

        Override o = new Override();
        o.setId(id);

        Map<String, String> parameters = new HashMap<String, String>(url.getParameters());

        o.setService(url.getServiceKey());
        parameters.remove(Constants.INTERFACE_KEY);
        parameters.remove(Constants.GROUP_KEY);
        parameters.remove(Constants.VERSION_KEY);
        parameters.remove(Constants.APPLICATION_KEY);
        parameters.remove(Constants.CATEGORY_KEY);
        parameters.remove(Constants.DYNAMIC_KEY);
        parameters.remove(Constants.ENABLED_KEY);

        o.setEnabled(url.getParameter(Constants.ENABLED_KEY, true));

        String host = url.getHost();
        boolean anyhost = url.getParameter(Constants.ANYHOST_VALUE, false);
        if (!anyhost || !"0.0.0.0".equals(host)) {
            o.setAddress(url.getAddress());
        }

        o.setApplication(url.getParameter(Constants.APPLICATION_KEY, url.getUsername()));
        parameters.remove(Constants.VERSION_KEY);

        o.setParams(StringUtils.toQueryString(parameters));

        return o;
    }

    /**
     * URL集合转成 Override 集合，其中 Long型的自增值作为 Override 对象的id
     *
     * @param cs
     * @return
     */
    public static List<Override> url2OverrideList(Map<Long, URL> cs) {
        List<Override> list = new ArrayList<>();
        if (cs == null) {
            return list;
        }
        for (Map.Entry<Long, URL> entry : cs.entrySet()) {
            list.add(url2Override(new Pair<>(entry.getKey(), entry.getValue())));
        }
        return list;
    }

    /**
     * 过滤category目录对应的 [Service下] 的 原子自增到URL的映射集合
     *
     * @param urls
     * @param filter
     * @param <SM>
     * @return
     */
    public static <SM extends Map<String, Map<Long, URL>>> Map<Long, URL> filterFromCategory(Map<String, SM> urls, Map<String, String> filter) {
        String c = filter.get(Constants.CATEGORY_KEY);
        if (c == null) {
            throw new IllegalArgumentException("no category");
        }

        filter.remove(Constants.CATEGORY_KEY);
        return filterFromService(urls.get(c), filter);
    }


    /**
     * 获取 Service层下的原子自增到URL的映射集合
     *
     * @param urls
     * @param filter
     * @return
     */
    public static Map<Long, URL> filterFromService(Map<String, Map<Long, URL>> urls, Map<String, String> filter) {
        Map<Long, URL> ret = new HashMap<>();
        if (urls == null) {
            return ret;
        }

        // service 过滤
        String s = filter.remove(SERVICE_FILTER_KEY);
        if (s == null) {
            for (Map.Entry<String, Map<Long, URL>> entry : urls.entrySet()) {
                filterFromUrls(entry.getValue(), ret, filter);
            }
        } else {
            Map<Long, URL> map = urls.get(s);
            filterFromUrls(map, ret, filter);
        }

        return ret;
    }

    /**
     * @param from
     * @param to
     * @param filter
     */
    static void filterFromUrls(Map<Long, URL> from, Map<Long, URL> to, Map<String, String> filter) {
        if (from == null || from.isEmpty()) {
            return;
        }

        for (Map.Entry<Long, URL> entry : from.entrySet()) {
            URL url = entry.getValue();

            boolean match = true;
            for (Map.Entry<String, String> e : filter.entrySet()) {
                String key = e.getKey();
                String value = e.getValue();
                // 地址过滤
                if (ADDRESS_FILTER_KEY.equals(key)) {
                    if (!value.equals(url.getAddress())) {
                        match = false;
                        break;
                    }
                    // 其他条件过滤
                } else {
                    if (!value.equals(url.getParameter(key))) {
                        match = false;
                        break;
                    }
                }
            }

            if (match) {
                to.put(entry.getKey(), url);
            }
        }
    }

    /**
     * @param urls     注册中心缓存信息
     * @param category 分类
     * @param id       模型对象的id，即原子自增的值
     * @param <SM>
     * @return
     */
    public static <SM extends Map<String, Map<Long, URL>>> Pair<Long, URL> filterFromCategory(Map<String, SM> urls, String category, Long id) {
        SM services = urls.get(category);
        if (services == null) {
            return null;
        }

        for (Map.Entry<String, Map<Long, URL>> e1 : services.entrySet()) {
            Map<Long, URL> u = e1.getValue();
            if (u.containsKey(id)) {
                return new Pair<>(id, u.get(id));
            }
        }

        return null;
    }
}
