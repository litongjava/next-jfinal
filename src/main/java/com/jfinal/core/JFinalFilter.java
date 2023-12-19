/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.core;

import java.io.IOException;

import com.jfinal.config.Constants;
import com.jfinal.config.JFinalConfig;
import com.jfinal.handler.Handler;
import com.jfinal.log.Log;
import com.jfinal.servlet.Filter;
import com.jfinal.servlet.FilterChain;
import com.jfinal.servlet.FilterConfig;
import com.jfinal.servlet.ServletException;
import com.jfinal.servlet.ServletRequest;
import com.jfinal.servlet.ServletResponse;
import com.jfinal.servlet.http.HttpServletRequest;
import com.jfinal.servlet.http.HttpServletResponse;

/**
 * JFinal framework filter
 */
public class JFinalFilter implements Filter {
	
	protected JFinalConfig jfinalConfig;
	protected int contextPathLength;
	protected Constants constants;
	protected String encoding;
	protected Handler handler;
	protected static Log log;
	
	protected static final JFinal jfinal = JFinal.me();
	
	public JFinalFilter() {
		this.jfinalConfig = null;
	}
	
	/**
	 * 支持 web 项目无需 web.xml 配置文件，便于嵌入式整合 jetty、undertow
	 */
	public JFinalFilter(JFinalConfig jfinalConfig) {
		this.jfinalConfig = jfinalConfig;
	}
	
	@SuppressWarnings("deprecation")
	public void init(FilterConfig filterConfig) throws ServletException {
		if (jfinalConfig == null) {
			createJFinalConfig(filterConfig.getInitParameter("configClass"));
		}
		
		jfinal.init(jfinalConfig, filterConfig.getServletContext());
		
		String contextPath = filterConfig.getServletContext().getContextPath();
		contextPathLength = (contextPath == null || "/".equals(contextPath) ? 0 : contextPath.length());
		
		constants = Config.getConstants();
		encoding = constants.getEncoding();
		
		jfinalConfig.onStart();
		jfinalConfig.afterJFinalStart();
		
		handler = jfinal.getHandler();		// 开始接受请求
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		request.setCharacterEncoding(encoding);
		
		String target = request.getRequestURI();
		if (contextPathLength != 0) {
			target = target.substring(contextPathLength);
		}
		
		boolean[] isHandled = {false};
		try {
			handler.handle(target, request, response, isHandled);
		}
		catch (Exception e) {
			if (log.isErrorEnabled()) {
				String qs = request.getQueryString();
				log.error(qs == null ? target : target + "?" + qs, e);
			}
		}
		
		if (isHandled[0] == false) {
			// 默认拒绝直接访问 jsp 文件，加固 tomcat、jetty 安全性
			if (constants.getDenyAccessJsp() && isJsp(target)) {
				com.jfinal.kit.HandlerKit.renderError404(request, response, isHandled);
				return ;
			}
			
			chain.doFilter(request, response);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void destroy() {
		handler = null;		// 停止接受请求
		
		jfinalConfig.onStop();
		jfinalConfig.beforeJFinalStop();
		
		jfinal.stopPlugins();
	}
	
	protected void createJFinalConfig(String configClass) {
		if (configClass == null) {
			throw new RuntimeException("The configClass parameter of JFinalFilter can not be blank");
		}
		
		try {
			Object temp = Class.forName(configClass).newInstance();
			jfinalConfig = (JFinalConfig)temp;
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Can not create instance of class: " + configClass, e);
		}
	}
	
	static void initLog() {
		log = Log.getLog(JFinalFilter.class);
	}
	
    boolean isJsp(String target) {
        int i = target.lastIndexOf('.');
        if (i > -1) {
            int len = target.length();
            i++;
            char c;
            if (i < len && ((c = target.charAt(i++)) == 'j' || c == 'J')) {
                if (i < len && ((c = target.charAt(i++)) == 's' || c == 'S')) {
                    if (i < len && ((c = target.charAt(i)) == 'p' || c == 'P')) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}





