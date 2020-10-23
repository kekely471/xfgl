
package com.tonbu.framework.util;

import com.tonbu.framework.data.Limit;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RequestUtils {
    protected static Logger logger = LogManager.getLogger(RequestUtils.class.getName());

    public RequestUtils() {
    }

    public static int getParam(HttpServletRequest req, String param, int defaultValue) {
        return NumberUtils.toInt(req.getParameter(param), defaultValue);
    }

    public static long getParam(HttpServletRequest req, String param, long defaultValue) {
        return NumberUtils.toLong(req.getParameter(param), defaultValue);
    }

    public static long[] getParamValues(HttpServletRequest req, String name) {
        String[] values = req.getParameterValues(name);
        return values == null ? null : (long[])ConvertUtils.convert(values, Long.TYPE);
    }

    public static String getParam(HttpServletRequest req, String param, String defaultValue) {
        String value = req.getParameter(param);
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }

    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        } else {
            Cookie[] var6 = cookies;
            int var5 = cookies.length;

            for(int var4 = 0; var4 < var5; ++var4) {
                Cookie ck = var6[var4];
                if (StringUtils.equalsIgnoreCase(name, ck.getName())) {
                    return ck;
                }
            }

            return null;
        }
    }

    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        } else {
            Cookie[] var6 = cookies;
            int var5 = cookies.length;

            for(int var4 = 0; var4 < var5; ++var4) {
                Cookie ck = var6[var4];
                if (StringUtils.equalsIgnoreCase(name, ck.getName())) {
                    return ck.getValue();
                }
            }

            return null;
        }
    }

    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, int maxAge) {
        setCookie(request, response, name, value, maxAge, true);
    }

    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, int maxAge, boolean all_sub_domain) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        if (all_sub_domain) {
            String serverName = request.getServerName();
            String domain = getDomainOfServerName(serverName);
            if (domain != null && domain.indexOf(46) != -1) {
                cookie.setDomain('.' + domain);
            }
        }

        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public static String getDomainOfServerName(String host) {
        if (isIPAddr(host)) {
            return null;
        } else {
            String[] names = StringUtils.split(host, '.');
            int len = names.length;
            if (len == 1) {
                return null;
            } else if (len == 3) {
                return makeup(names[len - 2], names[len - 1]);
            } else if (len <= 3) {
                return host;
            } else {
                String dp = names[len - 2];
                return !dp.equalsIgnoreCase("com") && !dp.equalsIgnoreCase("gov") && !dp.equalsIgnoreCase("net") && !dp.equalsIgnoreCase("edu") && !dp.equalsIgnoreCase("org") ? makeup(names[len - 2], names[len - 1]) : makeup(names[len - 3], names[len - 2], names[len - 1]);
            }
        }
    }

    public static boolean isIPAddr(String addr) {
        if (StringUtils.isEmpty(addr)) {
            return false;
        } else {
            String[] ips = StringUtils.split(addr, '.');
            if (ips.length != 4) {
                return false;
            } else {
                try {
                    int ipa = Integer.parseInt(ips[0]);
                    int ipb = Integer.parseInt(ips[1]);
                    int ipc = Integer.parseInt(ips[2]);
                    int ipd = Integer.parseInt(ips[3]);
                    return ipa >= 0 && ipa <= 255 && ipb >= 0 && ipb <= 255 && ipc >= 0 && ipc <= 255 && ipd >= 0 && ipd <= 255;
                } catch (Exception var6) {
                    return false;
                }
            }
        }
    }

    private static String makeup(String... ps) {
        StringBuilder s = new StringBuilder();

        for(int idx = 0; idx < ps.length; ++idx) {
            if (idx > 0) {
                s.append('.');
            }

            s.append(ps[idx]);
        }

        return s.toString();
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name, boolean all_sub_domain) {
        setCookie(request, response, name, "", 0, all_sub_domain);
    }

    public static Map<String, String> toMap(HttpServletRequest request) {
        Map<String, String> form = new HashMap();
        Enumeration<String> paramNames = request.getParameterNames();
        if (paramNames != null) {
            label30:
            while(true) {
                String paramName;
                String paramValue;
                do {
                    if (!paramNames.hasMoreElements()) {
                        break label30;
                    }

                    paramName = (String)paramNames.nextElement();
                    paramValue = request.getParameter(paramName);
                } while(StringUtils.isNotBlank(paramName) && (paramName.equals("_") || paramName.equals("page") || paramName.equals("rows") || paramName.equals("count")));

                form.put(paramName, paramValue);
            }
        }

        Limit limit = new Limit(request);
        form.put("startresult", String.valueOf(limit.getStartresult()));
        form.put("endresult", String.valueOf(limit.getEndresult()));
        form.put("totalresult", String.valueOf(limit.getTotalresult()));
        form.put("page", String.valueOf(limit.getStart()));
        form.put("rows", String.valueOf(limit.getSize()));
        form.put("count", String.valueOf(limit.getCount()));
        return form;
    }


    public static Map<String, Object> toObjectMap(HttpServletRequest request) {
        Map<String, Object> form = new HashMap();
        Enumeration<String> paramNames = request.getParameterNames();
        if (paramNames != null) {
            label30:
            while(true) {
                String paramName;
                String paramValue;
                do {
                    if (!paramNames.hasMoreElements()) {
                        break label30;
                    }

                    paramName = (String)paramNames.nextElement();
                    paramValue = request.getParameter(paramName);
                } while(StringUtils.isNotBlank(paramName) && (paramName.equals("_") || paramName.equals("page") || paramName.equals("rows") || paramName.equals("count")));

                form.put(paramName, paramValue);
            }
        }
        return form;
    }
}
