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
package com.alibaba.dubboadmin.web.pulltool;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubboadmin.governance.service.OverrideService;
import com.alibaba.dubboadmin.governance.service.RouteService;
import com.alibaba.dubboadmin.registry.common.domain.Consumer;
import com.alibaba.dubboadmin.registry.common.domain.Override;
import com.alibaba.dubboadmin.registry.common.domain.Provider;
import com.alibaba.dubboadmin.registry.common.domain.Route;
import com.alibaba.dubboadmin.registry.common.domain.User;
import com.alibaba.dubboadmin.registry.common.route.ParseUtils;
import com.alibaba.dubboadmin.registry.common.route.RouteRule;
import com.alibaba.dubboadmin.registry.common.route.RouteRule.MatchPair;
import com.alibaba.dubboadmin.registry.common.util.StringEscapeUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Tool
 */
@Component
public class Tool {

    @Autowired
    private OverrideService overrideService;
    @Autowired
    private RouteService routeService;

    public void setOverrideService(OverrideService overrideService) {
        this.overrideService = overrideService;
    }

    public void setRouteService(RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * 提供者是否可用
     *
     * @param provider 提供者
     * @param oList    配置
     * @return
     */
    public static boolean isProviderEnabled(Provider provider, List<Override> oList) {
        for (Override o : oList) {
            // 配置是否匹配当前提供者
            if (o.isMatch(provider)) {
                Map<String, String> params = StringUtils.parseQueryString(o.getParams());
                // 配置中的 disabled 属性值
                String disbaled = params.get(Constants.DISABLED_KEY);
                if (disbaled != null && disbaled.length() > 0) {
                    return !"true".equals(disbaled);
                }
            }
        }

        return provider.isEnabled();
    }

    /**
     * 获取提供者的权重
     *
     * @param provider 提供者
     * @param oList    配置
     * @return
     */
    public static int getProviderWeight(Provider provider, List<Override> oList) {
        for (Override o : oList) {
            // 配置是否匹配当前提供者
            if (o.isMatch(provider)) {
                Map<String, String> params = StringUtils.parseQueryString(o.getParams());
                // 配置中的 disabled 属性值
                String weight = params.get(Constants.WEIGHT_KEY);
                if (weight != null && weight.length() > 0) {
                    return Integer.parseInt(weight);
                }
            }
        }
        return provider.getWeight();
    }


    /**
     * 判断提供者是否可以用
     *
     * @param provider
     * @return
     */
    public boolean isProviderEnabled(Provider provider) {
        // 获取提供者的配置
        List<Override> oList = overrideService.findByServiceAndAddress(provider.getService(), provider.getAddress());
        // 如果提供者有配置信息，则先判断配置信息中是否设置了禁用提供者的属性。没有配置信息，则直接使用提供者的，默认情况是可用的。
        return isProviderEnabled(provider, oList);
    }

    /**
     * 获取提供者的权重
     *
     * @param provider
     * @return
     */
    public int getProviderWeight(Provider provider) {
        List<Override> oList = overrideService.findByServiceAndAddress(provider.getService(), provider.getAddress());
        return getProviderWeight(provider, oList);
    }


    /**
     * 判断消费者是否在黑名单中
     *
     * @param consumer
     * @return
     */
    public boolean isInBlackList(Consumer consumer) {

        String service = consumer.getService();

        // 查找服务对应的路由
        List<Route> routes = routeService.findForceRouteByService(service);
        if (routes == null || routes.size() == 0) {
            return false;
        }
        // 获取消费者ip
        String ip = getIP(consumer.getAddress());

        for (Route route : routes) {
            try {
                // 路由不可用直接返回
                if (!route.isEnabled()) {
                    continue;
                }

                // 获取过滤条件（过滤提供者）
                String filterRule = route.getFilterRule();
                // 过滤条件为空，或者值为false
                if (filterRule == null || filterRule.length() == 0 || "false".equals(filterRule)) {

                    // 解析匹配条件
                    Map<String, MatchPair> rule = RouteRule.parseRule(route.getMatchRule());

                    MatchPair pair = rule.get("consumer.host");

                    if (pair == null) {
                        pair = rule.get("host");
                    }

                    // 消费者主机匹配存在
                    if (pair != null) {

                        // 匹配条件存在
                        if (pair.getMatches() != null && pair.getMatches().size() > 0) {

                            // 判断消费者是否在设置的黑名单中（匹配路由中）
                            for (String host : pair.getMatches()) {
                                if (ParseUtils.isMatchGlobPattern(host, ip)) {
                                    return true;
                                }
                            }
                        }

                        // 不匹配条件存在
                        if (pair.getUnmatches() != null && pair.getUnmatches().size() > 0) {
                            boolean forbid = true;
                            for (String host : pair.getUnmatches()) {
                                if (ParseUtils.isMatchGlobPattern(host, ip)) {
                                    forbid = false;
                                }
                            }

                            if (forbid) {
                                return true;
                            }
                        }
                    }
                }

            } catch (ParseException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return false;
    }

    /**
     * 获取消费者的mock配置信息
     *
     * @param consumer
     * @return
     */
    public String getConsumerMock(Consumer consumer) {
        return getOverridesMock(consumer.getOverrides());
    }

    public String getOverridesMock(List<Override> overrides) {
        if (overrides != null && overrides.size() > 0) {
            for (Override override : overrides) {
                Map<String, String> params = StringUtils.parseQueryString(override.getParams());
                String mock = params.get("mock");
                if (mock != null && mock.length() > 0) {
                    return mock;
                }
            }
        }
        return "";
    }


    public String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.length() == 0) {
            return "";
        }
        return formatDate(new Date(Long.valueOf(timestamp)));
    }

    //format date
    public String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DateFormatUtil.getDateFormat().format(date);
    }

    public String formatDate(Date date, String template) {
        if (date == null || template == null) {
            return "";
        }
        return DateFormatUtil.getDateFormat(template).format(date);
    }

    public boolean beforeNow(Date date) {
        Date now = new Date();
        if (now.after(date)) {
            return true;
        }
        return false;
    }

    //minus of date
    public long dateMinus(Date date1, Date date2) {
        return (date1.getTime() - date1.getTime()) / 1000;
    }


    private static final Comparator<String> SIMPLE_NAME_COMPARATOR = new Comparator<String>() {

        @java.lang.Override
        public int compare(String s1, String s2) {
            if (s1 == null && s2 == null) {
                return 0;
            }
            if (s1 == null) {
                return -1;
            }
            if (s2 == null) {
                return 1;
            }
            s1 = getSimpleName(s1);
            s2 = getSimpleName(s2);
            return s1.compareToIgnoreCase(s2);
        }
    };

    public static String toStackTraceString(Throwable t) {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        t.printStackTrace(pw);
        return writer.toString();
    }

    public static boolean isContains(String[] values, String value) {
        return StringUtils.isContains(values, value);
    }

    public static boolean startWith(String value, String prefix) {
        return value.startsWith(prefix);
    }

    public static String getHostPrefix(String address) {
        if (address != null && address.length() > 0) {
            String hostname = getHostName(address);
            if (!address.startsWith(hostname)) {
                return "(" + hostname + ")";
            }
        }
        return "";
    }

    public static String getHostName(String address) {
        return NetUtils.getHostName(address);
    }

    public static String getHostAddress(String address) {
        if (address != null && address.length() > 0) {
            int i = address.indexOf(':');
            String port = address.substring(i + 1);
            String hostname = NetUtils.getHostName(address);
            if (!address.equals(hostname)) {
                return hostname + ":" + port;
            }
        }
        return "";
    }

    public static String getPath(String url) {
        try {
            return URL.valueOf(url).getPath();
        } catch (Throwable t) {
            return url;
        }
    }

    public static String getAddress(String url) {
        try {
            return URL.valueOf(url).getAddress();
        } catch (Throwable t) {
            return url;
        }
    }

    public static String getInterface(String service) {
        if (service != null && service.length() > 0) {
            int i = service.indexOf('/');
            if (i >= 0) {
                service = service.substring(i + 1);
            }
            i = service.lastIndexOf(':');
            if (i >= 0) {
                service = service.substring(0, i);
            }
        }
        return service;
    }

    public static String getGroup(String service) {
        if (service != null && service.length() > 0) {
            int i = service.indexOf('/');
            if (i >= 0) {
                return service.substring(0, i);
            }
        }
        return null;
    }

    public static String getVersion(String service) {
        if (service != null && service.length() > 0) {
            int i = service.lastIndexOf(':');
            if (i >= 0) {
                return service.substring(i + 1);
            }
        }
        return null;
    }

    public static String getIP(String address) {
        if (address != null && address.length() > 0) {
            int i = address.indexOf("://");
            if (i >= 0) {
                address = address.substring(i + 3);
            }
            i = address.indexOf('/');
            if (i >= 0) {
                address = address.substring(0, i);
            }
            i = address.indexOf('@');
            if (i >= 0) {
                address = address.substring(i + 1);
            }
            i = address.indexOf(':');
            if (i >= 0) {
                address = address.substring(0, i);
            }
            if (address.matches("[a-zA-Z]+")) {
                try {
                    address = InetAddress.getByName(address).getHostAddress();
                } catch (UnknownHostException e) {
                }
            }
        }
        return address;
    }

    public static String encode(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

    public static String escape(String html) {
        return StringEscapeUtils.escapeHtml(html);
    }

    public static String unescape(String html) {
        return StringEscapeUtils.unescapeHtml(html);
    }

    public static String encodeUrl(String url) {
        return URL.encode(url);
    }

    public static String decodeUrl(String url) {
        return URL.decode(url);
    }

    public static String encodeHtml(String html) {
        return StringEscapeUtils.escapeHtml(html);
    }

    public static int countMapValues(Map<?, ?> map) {
        int total = 0;
        if (map != null && map.size() > 0) {
            for (Object value : map.values()) {
                if (value != null) {
                    if (value instanceof Number) {
                        total += ((Number) value).intValue();
                    } else if (value.getClass().isArray()) {
                        total += Array.getLength(value);
                    } else if (value instanceof Collection) {
                        total += ((Collection<?>) value).size();
                    } else if (value instanceof Map) {
                        total += ((Map<?, ?>) value).size();
                    } else {
                        total += 1;
                    }
                }
            }
        }
        return total;
    }

    public static List<String> sortSimpleName(List<String> list) {
        if (list != null && list.size() > 0) {
            Collections.sort(list, SIMPLE_NAME_COMPARATOR);
        }
        return list;
    }

    public static String getSimpleName(String name) {
        if (name != null && name.length() > 0) {
            final int ip = name.indexOf('/');
            String v = ip != -1 ? name.substring(0, ip + 1) : "";

            int i = name.lastIndexOf(':');
            int j = (i >= 0 ? name.lastIndexOf('.', i) : name.lastIndexOf('.'));
            if (j >= 0) {
                name = name.substring(j + 1);
            }
            name = v + name;
        }
        return name;
    }

    public static String getParameter(String parameters, String key) {
        String value = "";
        if (parameters != null && parameters.length() > 0) {
            String[] pairs = parameters.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (key.equals(kv[0])) {
                    value = kv[1];
                    break;
                }
            }
        }
        return value;
    }

    public static Map<String, String> toParameterMap(String parameters) {
        return StringUtils.parseQueryString(parameters);
    }

    /**
     * Get the version value from the paramters parameter of provider
     *
     * @param parameters
     * @return
     */
    public static String getVersionFromPara(String parameters) {
        String version = "";
        if (parameters != null && parameters.length() > 0) {
            String[] params = parameters.split("&");
            for (String o : params) {
                String[] kv = o.split("=");
                if ("version".equals(kv[0])) {
                    version = kv[1];
                    break;
                }
            }
        }
        return version;
    }

    public boolean checkUrl(User user, String uri) {
        return true;
    }
}
