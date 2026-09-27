-injars build/libs/duskbyte_obf.jar
-outjars build/libs/duskbyte_obf.jar
-libraryjars <java.home>/lib/jrt-fs.jar

# 关键修复:必须保留这些 class 文件属性,否则 Mixin/Fabric 在运行时找不到
# @Mixin、@Inject、@Environment 等注解,直接崩溃("missing an @Mixin annotation")。
# Signature 同样要保留,否则涉及泛型的 Mixin 注入点解析也会出问题。
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations,AnnotationDefault,Signature,InnerClasses,EnclosingMethod,Exceptions

-dontshrink
-dontoptimize

-keep public class dev.duskbyte.Main { *; }
-keep class dev.duskbyte.mixin.** { *; }
-keep class dev.duskbyte.imixin.** { *; }
-keep class dev.duskbyte.DuskByte { public *; protected *; public static *; }
-keep class dev.duskbyte.event.** { *; }
-keep class dev.duskbyte.module.modules.** { public <init>(); }
-keep class dev.duskbyte.gui.** { public <init>(...); }
-keep class dev.duskbyte.managers.cloud.** { *; }
-keep class dev.duskbyte.managers.AuthManager { *; }
-keep class dev.duskbyte.managers.TranslationManager { *; }

-keep class net.fabricmc.** { *; }
-keep class com.google.gson.** { *; }

-optimizationpasses 3
-repackageclasses
-allowaccessmodification
-renamesourcefileattribute SourceFile
-printmapping build/libs/mapping.txt
