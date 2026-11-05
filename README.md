# Avaritia Tinkers Fix

一个小型客户端 addon，修复：用 CodeChickenLib `IItemRenderer` 渲染的物品（无尽贪婪的 halo 物品等）放进匠魂怀古的工具台 / 工具锻造台后，台面上显示紫黑缺失模型方块的问题，同时保留这些物品在 GUI 里的光晕与脉冲特效。



## 构建

需要 **JDK 25** 运行 Gradle（RFG 2.0.3 的要求；项目本身用 Java 8 toolchain 编译），并确保 `JAVA_HOME` 指向它。

```bash
# Linux / macOS / Git Bash
./gradlew build

# Windows CMD
gradlew.bat build

# Windows PowerShell
.\gradlew.bat build
```

首次构建会自动从 CleanroomMC Maven 和 CurseMaven 下载依赖。

