package com.jfinal.plugin.ehcache;

/**
 * IDataLoader.
 * <p>
 * Example:
 * <pre>
 * List<Blog> blogList = EhCacheKit.handle("blog", "blogList", new IDataLoader(){
 *     public Object load() {
 *         return Blog.dao.find("select * from blog");
 * }});
 * </pre>
 */
@FunctionalInterface
public interface IDataLoader {
  public Object load();
}
