-injars build/libs/duskbyte_obf.jar
-outjars build/libs/duskbyte_obf.jar
-libraryjars <java.home>/lib/jrt-fs.jar

# 背景: 如果注解在混淆时被裁剪,运行时会报 "missing an @Mixin annotation" 崩溃,
# 因此曾尝试添加 -keepattributes —— 但不能这么写:
# ZKM 27.0.0 的 ProGuard 翻译器会把它翻译成
# "keepAnnotations" 选项,但该版本脚本解析器不接受该选项,会直接抛
# ZkmScriptParseException(While parsing "obfuscate" statement),导致 ZKM 无法启动。
# 已启用 -dontshrink/-dontoptimize,ZKM 只做重命名,不会裁剪注解属性;
# CI 的 "Diagnose annotation stripping" 步骤会用 javap 校验混淆后
# @Mixin 等 RuntimeVisibleAnnotations 是否仍然保留。

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
