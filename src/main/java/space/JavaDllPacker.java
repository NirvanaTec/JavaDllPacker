/*
 * 涅槃科技 and 风横
 * https://npyyds.top/
 * https://gitee.com/newNP/
 * https://github.com/NirvanaTec/
 * 最终解释权归涅槃科技所有，涅槃科技版权所有。
 */
package space;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JavaDllPacker {

    public static void main(String[] args) {
        if (args.length < 1) {

            JFrame frame = new JFrame("涅槃科技 \u00a9 版权所有 npyyds.top Today !");
            frame.setSize(400, 300);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JLabel label = new JLabel("请将文件拖入以加载！", JLabel.CENTER);
            frame.add(label);

            // 设置拖拽监听器
            frame.setTransferHandler(new TransferHandler() {
                @Override
                public boolean canImport(TransferSupport support) {
                    return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
                }

                @Override
                public boolean importData(TransferSupport support) {
                    try {
                        Object file = support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        List<File> files = new ArrayList<>();
                        if (file instanceof List<?> list) {
                            for (Object o : list) {
                                if (o instanceof File) {
                                    files.add((File) o);
                                }
                            }
                        }
                        if (!files.isEmpty()) {
                            if (dispose(files.getFirst().getAbsolutePath())) {
                                JOptionPane.showMessageDialog(null, "完成!");
                                System.exit(0);
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        e.fillInStackTrace();
                        return false;
                    }
                }
            });

            frame.setVisible(true);
            return;
        }
        if (dispose(args[0])) {
            System.out.println("ok");
        }
    }

    public static boolean dispose(String args) {
        try {
            List<ClassInfo> list = init(args);
            if (list == null) {
                return false;
            }
            StringBuilder text = new StringBuilder("""
                    <火山程序 类型 = "通常" 版本 = 1 />

                    包 JNI.模组解析 <注释 = "由涅槃科技提供技术支持" 注释 = " https://npyyds.top/" 注释 = " https://github.com/NirvanaTec"
                            注释 = " https://gitee.com/newNP/SpaceLoader" 注释 = " 涅槃科技 and 风横 \u00a9 3547694806">

                    类 工具_模组 <公开 @全局类 = 真>
                    {
                        变量 模组_类内容 <公开 静态 类型 = 字节集数组类>
                        变量 模组_类名称 <公开 静态 类型 = 文本数组类>
                        变量 模组_数量 <公开 静态 类型 = 整数 值 =\s""").append(list.size() - 1);
            text.append("""
                    >
                        变量 模组_Loader <公开 静态 类型 = 字节集类>
                        变量 模组_InjectionEndpoint <公开 静态 类型 = 字节集类>
                    """);
            int count = 0;
            for (int i = 0; i < list.size(); i++) {
                ClassInfo classInfo = list.get(i);
                String name;
                if (classInfo.name1.equals("space.loader.Loader")) {
                    count++;
                    name = "模组_原Loader";
                } else if (classInfo.name1.equals("space.loader.InjectionEndpoint")) {
                    count++;
                    name = "模组_原InjectionEndpoint";
                } else {
                    name = "文件" + i;
                }
                text.append("    变量 ").append(name).append(" <静态 类型 = 视窗文件资源\n").append("            值 = \"").append(classInfo.path).append("\">\n");
            }

            if (count != 2) {
                JOptionPane.showMessageDialog(null, "异常! 这可能是不支持的模组!");
                return false;
            }

            text.append("""

                        方法 模组_初始化模组 <公开 静态 类型 = 字节集数组类>
                        {
                            变量 局_字节 <类型 = 字节集类>
                            文件资源到字节集 (模组_原Loader, 模组_Loader)
                            文件资源到字节集 (模组_原InjectionEndpoint, 模组_InjectionEndpoint)
                    """);

            for (int i = 0; i < list.size(); i++) {
                ClassInfo classInfo = list.get(i);
                if (classInfo.name1.equals("space.loader.Loader") || classInfo.name1.equals("space.loader.InjectionEndpoint")) {
                    continue;
                }
                text.append("        文件资源到字节集 (文件").append(i).append(", 局_字节)\n").append("        模组_类名称.加入成员 (\"").append(classInfo.name1).append("\")\n").append("        模组_类内容.加入成员 (局_字节)\n");
            }
            text.append("        模组_类名称.加入成员 (\"space.loader.InjectionEndpoint\")\n").append("        模组_类内容.加入成员 (模组_InjectionEndpoint)\n");
            text.append("""
                            返回 (模组_类内容)
                        }
                    }
                    """);
            FileOutputStream fos = new FileOutputStream("SpaceLoader/SpaceLoader.wsv");
            fos.write(text.toString().getBytes(StandardCharsets.UTF_16LE));
            fos.close();
            return true;
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return false;
    }

    public static List<ClassInfo> init(String args) throws Exception {

        // 创建 SpaceLoader 目录
        File file = new File("SpaceLoader/");
        if (!file.mkdirs() && !file.exists()) {
            System.out.println("创建 \"SpaceLoader\" 目录失败!");
            return null;
        }

        // 移除 SpacePacker 目录
        file = new File("SpaceLoader/SpacePacker/");
        if (file.exists()) {
            if (!deleteDirectory(file)) {
                System.out.println("移除 \"SpacePacker\" 目录失败!");
                return null;
            }
        }

        // 创建 SpacePacker 目录
        if (!file.mkdirs() && !file.exists()) {
            System.out.println("新建 \"SpacePacker\" 目录失败!");
            return null;
        }

        // 以 ZIP 的方式打开模组
        ZipInputStream zipFile = new ZipInputStream(Files.newInputStream(Paths.get(args)));
        List<ClassPair> classes = new ArrayList<>();
        HashMap<String, ClassPair> classMap = new HashMap<>();

        while (true) {
            ZipEntry entry; // 单个文件
            do {
                entry = zipFile.getNextEntry(); // 获取单个文件
                if (entry != null) {
                    continue;
                }
                classes.forEach((_) -> classes.forEach((cPair) -> {
                    ClassInfo cInfo = cPair.classInfo;
                    if (classMap.containsKey(cInfo.superClass)) {
                        classMap.get(cInfo.superClass).priority = Math.max(classMap.get(cInfo.superClass).priority, cPair.priority + 1);
                    }

                    String[] var3 = cInfo.interfaces;

                    for (String iFace : var3) {
                        if (classMap.containsKey(iFace)) {
                            classMap.get(iFace).priority = Math.max(classMap.get(iFace).priority, cPair.priority + 1);
                        }
                    }

                }));

                Collections.sort(classes); // 自动排序
                ArrayList<ClassInfo> list = new ArrayList<>();
                for (ClassPair cPair : classes) {
                    list.add(cPair.classInfo);
                }
                return list; // 真返回
            } while (!entry.getName().endsWith(".class"));

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[16384];
            String path = "SpaceLoader/SpacePacker/" + entry.getName().replace("$", "_dollar_");
            // 自动创建目录
            Files.createDirectories(Paths.get(path).getParent());
            // 写出文件
            FileOutputStream fos = new FileOutputStream(path);
            int nRead;
            while ((nRead = zipFile.read(data, 0, data.length)) != -1) {
                fos.write(data, 0, nRead); // 写出文件
                buffer.write(data, 0, nRead);
            }
            fos.close(); // 关闭写出流
            byte[] classData = buffer.toByteArray();
            classes.add(new ClassPair(classData));
            ClassInfo cInfo = new ClassInfo(classes.getLast().classData);
            cInfo.path = "SpacePacker\\\\" + entry.getName().replace("/", "\\\\").replace("$", "_dollar_"); // 保存路径
            classes.getLast().classInfo = cInfo;
            classMap.put(cInfo.name1, classes.getLast());
        }
    }

    public static boolean deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file); // 递归删除子目录
                } else {
                    if (!file.delete()) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

}