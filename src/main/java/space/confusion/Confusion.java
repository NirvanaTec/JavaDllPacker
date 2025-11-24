package space.confusion;

import space.calculate.ClassInfo;
import space.utils.Tools;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

// 混淆类
public class Confusion {

    public static String zkmChangeLog;
    public static String mode;
    public static ClassInfo loader;
    public static ClassInfo loaderInjection;

    // 根据 changeLog.txt 判断
    public static boolean isZkm(ArrayList<ClassInfo> list, JLabel label) {

        // changeLog.txt 文件不存在
        File changeLogFile = new File("changeLog.txt");
        if (!changeLogFile.exists()) {
            return false;
        }

        // 文件 changeLog.txt 内容
        String changeLog;
        try {
            changeLog = Files.readString(changeLogFile.toPath());
        } catch (IOException e) {
            label.setText("读取 changeLog.txt 失败！");
            return false;
        }

        // changeLog.txt 内容是否包含所有类
        for (ClassInfo classInfo : list) {
            // 在 changeLog.txt 中不存在该类
            if (!changeLog.contains(classInfo.name1)) {
                return false;
            } else if (classInfo.path.endsWith("/Loader.class")) {
                loader = classInfo;
            } else if (classInfo.path.endsWith("/LoaderInjection.class")) {
                loaderInjection = classInfo;
            }
        }
        zkmChangeLog = changeLog;
        // 复制到 changeLog1.txt
        try {
            Tools.saveFile("changeLog1.txt", changeLog, StandardCharsets.UTF_8);
        } catch (IOException e) {
            label.setText("备份 changeLog.txt 失败！");
            return false;
        }

        changeLogFile.delete();
        return true;
    }

    // 检测是否为混淆类
    public static void execute(ArrayList<ClassInfo> list, JLabel label) {
        if (isZkm(list, label)) {
            mode = "zkm";
            label.setText("检测到 zkm 混淆！");
        }
    }

    // 返回结果
    public static Entity execute() {
        Entity entity = new Entity();
        if ("zkm".equals(mode)) {
            String text;

            // loader.Loader - startName
            text = Tools.getTextToRight(zkmChangeLog, loader.name1);
            text = Tools.getTextToRight(text, "public static int a(byte[][]");
            entity.startName = Tools.getTextBetween(text, "\t=>\t",  "(byte[][]");

            // loader.Loader - byteName
            text = Tools.getTextToRight(zkmChangeLog, loader.name1);
            text = Tools.getTextToRight(text, "public static byte[][] a(int)");
            entity.byteName = Tools.getTextBetween(text, "\t=>\t",  "(int)");
        }
        return entity;
    }

}
