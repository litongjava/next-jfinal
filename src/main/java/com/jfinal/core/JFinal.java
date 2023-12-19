/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.jfinal.core;

import java.util.List;

import com.jfinal.config.Constants;
import com.jfinal.config.JFinalConfig;
import com.jfinal.handler.Handler;
import com.jfinal.handler.HandlerFactory;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.render.RenderManager;
import com.jfinal.server.IServer;
import com.jfinal.servlet.ServletContext;
import com.jfinal.token.ITokenCache;
import com.jfinal.token.TokenManager;
import com.jfinal.upload.UploadConfig;

/**
 * JFinal
 */
public final class JFinal {

  private Constants constants;
  private ActionMapping actionMapping;
  private Handler handler;
  private ServletContext servletContext;
  private String contextPath = "";
  private static IServer server;

  private static final JFinal me = new JFinal();

  private JFinal() {
  }

  public static JFinal me() {
    return me;
  }

  public void init(JFinalConfig jfinalConfig, ServletContext servletContext) {
    this.servletContext = servletContext;
    this.contextPath = servletContext.getContextPath();

    initPathKit();

    Config.configJFinal(jfinalConfig); // start plugin, init log factory and init engine in this method
    constants = Config.getConstants();

    initActionMapping();
    initHandler();
    initRender();
    initUploadConfig();
    initTokenManager();

  }

  private void initTokenManager() {
    ITokenCache tokenCache = constants.getTokenCache();
    if (tokenCache != null) {
      TokenManager.init(tokenCache);
    }
  }

  private void initHandler() {
    ActionHandler actionHandler = Config.getHandlers().getActionHandler();
    if (actionHandler == null) {
      actionHandler = new ActionHandler();
    }

    actionHandler.init(actionMapping, constants);
    handler = HandlerFactory.getHandler(Config.getHandlers().getHandlerList(), actionHandler);
  }

  private void initUploadConfig() {
    UploadConfig.init(constants.getBaseUploadPath(), constants.getMaxPostSize(), constants.getEncoding());
  }

  private void initPathKit() {
    String path = servletContext.getRealPath("/");
    PathKit.setWebRootPath(path);
  }

  private void initRender() {
    RenderManager.me().init(Config.getEngine(), constants, servletContext);
  }

  private void initActionMapping() {
    if (constants.getActionMappingFunc() != null) {
      actionMapping = constants.getActionMappingFunc().apply(Config.getRoutes());
    } else {
      actionMapping = new ActionMapping(Config.getRoutes());
    }

    actionMapping.buildActionMapping();
    Config.getRoutes().clear();
  }

  public void stopPlugins() {
    List<IPlugin> plugins = Config.getPlugins().getPluginList();
    if (plugins != null) {
      for (int i = plugins.size() - 1; i >= 0; i--) { // stop plugins
        boolean success = false;
        try {
          success = plugins.get(i).stop();
        } catch (Exception e) {
          success = false;
          LogKit.error(e.getMessage(), e);
        }
        if (!success) {
          System.err.println("Plugin stop error: " + plugins.get(i).getClass().getName());
        }
      }
    }
  }

  public Handler getHandler() {
    return handler;
  }

  public Constants getConstants() {
    return Config.getConstants();
  }

  public String getContextPath() {
    return contextPath;
  }

  public ServletContext getServletContext() {
    return this.servletContext;
  }

  public Action getAction(String url, String[] urlPara) {
    return actionMapping.getAction(url, urlPara);
  }

  public List<String> getAllActionKeys() {
    return actionMapping.getAllActionKeys();
  }
}
