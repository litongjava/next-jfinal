package com.jfinal.proxy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ProxyClassTemplate {

  String filename = "proxy_class_template.jf";

  public ProxyClassTemplate() {
  }

  public ProxyClassTemplate(String filename) {
    this.filename = filename;
  }

  public void create(String content) {
    try {
      // 使用 BufferedWriter 和 FileWriter
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
        writer.write(content);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void create() {
    String content = buildTemplateContent(); // 这个方法将返回你要写入文件的内容
    create(content);
  }

  private String buildTemplateContent() {
    return "#--\n"
           + "生成的源代码格式如下：\n"
           + "\n"
           + "package com.xxx;\n"
           + "import com.litongjava.jfinal.aop.Invocation;\n"
           + "\n"
           + "public class Target$$EnhancerByJFinal extends Target {\n"
           + "\tpublic String test(String p0, int p1) {\n"
           + "\t\tInvocation inv = new Invocation(this, 123L,\n"
           + "\t\t\targs -> {\n"
           + "\t\t\t\treturn super.test(\n"
           + "\t\t\t\t\t\t(String)args[0],\n"
           + "\t\t\t\t\t\t(int)args[1]\n"
           + "\t\t\t\t);\n"
           + "\t\t\t},\n"
           + "\t\t\tp0, p1);\n"
           + "\t\t\n"
           + "\t\tinv.invoke();\n"
           + "\t\t\n"
           + "\t\treturn inv.getReturnValue();\n"
           + "\t}\n"
           + "}\n"
           + "--#\n"
           + "\n"
           + "package #(pkg);\n"
           + "import com.litongjava.jfinal.aop.Invocation;\n"
           + "public class #(name)#(classTypeVars) extends #(targetName)#(targetTypeVars) {\n"
           + "#for(x : methodList)\n"
           + "\t\n"
           + "\tpublic #(x.methodTypeVars) #(x.returnType) #(x.name)(#for(y : x.paraTypes)#(y) p#(for.index)#(for.last ? \"\" : \", \")#end) #(x.throws){\n"
           + "\t\t#if(x.singleArrayPara)\n"
           + "\t\t#@newInvocationForSingleArrayPara()\n"
           + "\t\t#else\n"
           + "\t\t#@newInvocationForCommon()\n"
           + "\t\t#end\n"
           + "\t\t\n"
           + "\t\tinv.invoke();\n"
           + "\t\t#if (x.returnType != \"void\")\n"
           + "\t\t\n"
           + "\t\treturn inv.getReturnValue();\n"
           + "\t\t#end\n"
           + "\t}\n"
           + "#end\n"
           + "}\n"
           + "\n"
           + "#--\n"
           + "一般参数情况\n"
           + "--#\n"
           + "#define newInvocationForCommon()\n"
           + "\tInvocation inv = new Invocation(this, #(x.proxyMethodKey)L,\n"
           + "\t\targs -> {\n"
           + "\t\t\t#(x.frontReturn) #(name).super.#(x.name)(\n"
           + "\t\t\t\t#for(y : x.paraTypes)\n"
           + "\t\t\t\t(#(y.replace(\"...\", \"[]\")))args[#(for.index)]#(for.last ? \"\" : \",\")\n"
           + "\t\t\t\t#end\n"
           + "\t\t\t);\n"
           + "\t\t\t#(x.backReturn)\n"
           + "\t\t}\n"
           + "\t\t#for(y : x.paraTypes), p#(for.index)#end);\n"
           + "#end\n"
           + "#--\n"
           + "只有一个参数，且该参数是数组或者可变参数\n"
           + "--#\n"
           + "#define newInvocationForSingleArrayPara()\n"
           + "\tInvocation inv = new Invocation(this, #(x.proxyMethodKey)L,\n"
           + "\t\targs -> {\n"
           + "\t\t\t#(x.frontReturn) #(name).super.#(x.name)(\n"
           + "\t\t\t\tp0\n"
           + "\t\t\t);\n"
           + "\t\t\t#(x.backReturn)\n"
           + "\t\t}\n"
           + "\t\t, p0);\n"
           + "#end\n";
    }
}
