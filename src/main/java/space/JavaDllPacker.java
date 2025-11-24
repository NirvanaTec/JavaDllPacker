/*
 * 涅槃科技 and 风横
 * https://npyyds.top/
 * https://gitee.com/newNP/
 * https://github.com/NirvanaTec/
 * 最终解释权归涅槃科技所有，涅槃科技版权所有。
 */
package space;

import com.alibaba.fastjson2.JSONObject;
import space.calculate.ClassInfo;
import space.calculate.ClassPair;
import space.confusion.Confusion;
import space.confusion.Entity;
import space.pass.PassInfo;
import space.utils.Tools;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JavaDllPacker {

    // 开启多重加密
    public static final boolean isMultiEncrypt = true;

    public static void main(String[] args) throws Exception {

        // 是否为排序模式
        if (args.length > 0) {
            dispose(args[0], null);
            return;
        }

        // 创建窗口
        JFrame frame = new JFrame("涅槃科技 © 版权所有 npyyds.top Today !");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 设置布局管理器为
        frame.setLayout(new BorderLayout());

        // 添加 拖拽 标签
        JLabel label = new JLabel("请将文件拖入以加载！", JLabel.CENTER);
        frame.add(label, BorderLayout.CENTER);

        // 添加 倒计时 标签
        JLabel label1 = new JLabel("", JLabel.LEFT); // 将标签内容设置为左对齐
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        southPanel.add(label1);
        frame.add(southPanel, BorderLayout.SOUTH);

        // 设置拖拽监听器
        frame.setTransferHandler(new FileTransferHandler(label, label1));
        // 显示窗口
        frame.setVisible(true);

    }

    // 输出类加载顺序
    public static void dispose(String args, JLabel label) throws Exception {
        dispose(Paths.get(args), label);
    }

    // 输出类加载顺序
    public static void dispose(Path args, JLabel label) throws Exception {
        List<ClassInfo> list = init(args, label);
        StringBuilder text = new StringBuilder();
        for (ClassInfo classInfo : list) {
            text.append(classInfo.path).append("[#@@#]");
        }
        System.out.println(text);
    }

    public static List<ClassInfo> init(Path args, JLabel label) throws Exception {

        // 移除 SpacePacker 目录, 但不移除 libs 文件夹
        File file = new File("SpacePacker/");
        // 存在 并且 删除失败
        if (file.exists() && !Tools.deleteDirectory(file, false)) {
            throw new Exception("移除 \"SpacePacker\" 目录失败!");
        }

        // 创建 SpacePacker 目录
        // 创建目录失败 并且 目录目不存在
        if (!file.mkdirs() && !file.exists()) {
            throw new Exception("新建 \"SpacePacker\" 目录失败!");
        }

        return execute(args, label);
    }

    public static List<ClassInfo> execute(Path args, JLabel label) throws Exception {

        try (ZipInputStream zipFile = new ZipInputStream(Files.newInputStream(args))) {
            // .class 文件列表
            List<ClassPair> classes = new ArrayList<>();
            // 类信息映射表
            HashMap<String, ClassPair> classMap = new HashMap<>();
            // 单个文件
            ZipEntry entry;

            // 1. 读取并解析所有.class文件
            while ((entry = zipFile.getNextEntry()) != null) {
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue; // 跳过目录和非class文件
                }

                // 读取 class 内容到缓冲区
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[16384];

                // 自动创建目录
                String path = "SpacePacker/" + entry.getName().replace("$", "_dollar_");
                Files.createDirectories(Paths.get(path).getParent());
                // 写出文件
                FileOutputStream fos = new FileOutputStream(path);

                int nRead;
                while ((nRead = zipFile.read(data, 0, data.length)) != -1) {
                    fos.write(data, 0, nRead);
                    buffer.write(data, 0, nRead);
                }
                fos.close(); // 关闭写出流
                byte[] classData = buffer.toByteArray();

                // 创建 ClassPair 并解析类信息
                ClassPair cPair = new ClassPair(classData);
                ClassInfo cInfo = new ClassInfo(classData); // 假设 ClassInfo 能解析字节码
                cInfo.path = entry.getName().replace("$", "_dollar_");
                cPair.classInfo = cInfo;

                classes.add(cPair);
                classMap.put(cInfo.name1, cPair); // 假设name1是全限定类名
            }

            if (classes.isEmpty()) {
                throw new Exception("没有找到任何.class文件！");
            }

            // 2. 计算优先级（基于继承层次）
            for (ClassPair cPair : classes) {
                ClassInfo cInfo = cPair.classInfo;
                // 处理父类
                if (classMap.containsKey(cInfo.superClass)) {
                    classMap.get(cInfo.superClass).priority = Math.max(
                            classMap.get(cInfo.superClass).priority, cPair.priority + 1);
                }
                // 处理接口
                for (String iFace : cInfo.interfaces) {
                    if (classMap.containsKey(iFace)) {
                        classMap.get(iFace).priority = Math.max(
                                classMap.get(iFace).priority, cPair.priority + 1);
                    }
                }
            }

            // 3. 排序并返回结果
            Collections.sort(classes); // 需要 ClassPair 实现 Comparable

            ArrayList<ClassInfo> result = new ArrayList<>();
            for (ClassPair cPair : classes) {
                result.add(cPair.classInfo);
            }

            // 混淆检测
            Confusion.execute(result, label);
            // 生成 wsv 文件
            generateWsv(result);
            return result;
        }
    }

    // 生成 wsv 文件
    public static void generateWsv(ArrayList<ClassInfo> list) throws IOException {
        // 返回内容

        // 获取 resources.config.json 配置文件
        try (InputStream inputStream = JavaDllPacker.class.getClassLoader().getResourceAsStream("config.json")) {
            if (inputStream == null) {
                throw new FileNotFoundException("config.json 文件未找到");
            }
            // 读取 JSON 文件内容
            String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            // 解析 JSON 字符串为 JSONObject
            JSONObject jsonObject = JSONObject.parseObject(jsonContent);

            // wsv 内容
            StringBuilder initText = new StringBuilder(); // SpaceLoader.wsv

            // encrypt 配置
            JSONObject jsonConfig = new JSONObject(); // encrypt.json
            StringBuilder encryptText = new StringBuilder(); // SpaceLoader.wsv

            // 获取 head 字段值
            initText.append(jsonObject.getString("head"));

            // 模组索引
            int index = 0;
            int passIndex = 0;
            for (ClassInfo cInfo : list) {
                index++;
                // 把 类 加密后的密码
                String pass = "-1";
                if (isMultiEncrypt) {
                    // 获取密码
                    pass = generatePass(cInfo);

                    // 密码保存到文件
                    jsonConfig.put(cInfo.path, pass);

                    // 加密开始
                    PassInfo passInfo = executePass(passIndex, jsonObject, pass);
                    encryptText.append(passInfo.text);

                    // 序列增加
                    passIndex += pass.length() + 1;

                    // 替换密码
                    pass = passInfo.name;
                }

                if (cInfo.path.endsWith("/Loader.class")) {
                    encryptText.append(executeConfig(cInfo, jsonObject.getString("loader"), index, pass));
                    continue;
                }

                // 单个类
                initText.append(executeConfig(cInfo, jsonObject.getString("text"), index, pass));
                if (isMultiEncrypt){
                    initText.append(executeConfig(cInfo, jsonObject.getString("pass"), index, pass));
                }
                initText.append(executeConfig(cInfo, jsonObject.getString("text1"), index, pass));
            }

            initText.append(jsonObject.getString("textTail")).append(encryptText).append(jsonObject.getString("tail"));

            PassInfo passInfo = new PassInfo(initText);
            passInfo.setText("size", list.size());

            // 取混淆信息
            Entity entity = Confusion.execute();
            passInfo.setText("startName", entity.startName); // 启动名称
            passInfo.setText("byteName", entity.byteName); // 新数组名称

            // 保存文件
            // 保存 wsv 文件
            Tools.saveFile("SpacePacker\\SpaceLoader.wsv", passInfo.text, StandardCharsets.UTF_16LE);
            // 保存 encrypt.json 文件
            Tools.saveFile("SpacePacker\\encrypt.json", jsonConfig.toString(), StandardCharsets.UTF_8);
        }

    }

    // 生成随机密码
    // ClassInfo 可选
    // 格式：4位时间戳 + 4位随机数 + 4位随机字母 + 4位随机字母 + 类名（取.的后面）
    public static String generatePass(ClassInfo text) {
        SecureRandom random = new SecureRandom();
        StringBuilder pass = new StringBuilder(String.valueOf(System.currentTimeMillis()));

        // 取右4位
        pass = new StringBuilder(pass.substring(pass.length() - 4));

        for (int i = 0; i < 4; i++) {
            // 4位随机数
            pass.append(random.nextInt(10));
            // 4位随机字母
            pass.append((char) ('a' + random.nextInt(26)));
            // 4位随机字母
            pass.append((char) ('A' + random.nextInt(26)));
        }

        if (text != null){
            String className = text.name1;
            if (className != null && className.contains(".")){
                // 取.的后面
                className = className.substring(className.lastIndexOf(".") + 1);
                pass.append(className);
            }
        }

        return pass.toString();
    }

    public static PassInfo executePass(int index, JSONObject jsonObject, String pass) {

        PassInfo passInfo = new PassInfo();
        index++;
        passInfo.name = "解密过程_" + index;
        passInfo.text = jsonObject.getString("passHead");
        passInfo.setText("index", passInfo.name);

        PassInfo passInfo1 = new PassInfo();
        for (char c : pass.toCharArray()){
            // 解密过程
            passInfo.addText(jsonObject.getString("passText"));
            passInfo.setText("index", ++index);
            // 加密过程
            passInfo1.addText(jsonObject.getString("passIn"));
            passInfo1.setText("index", index);
            passInfo1.setText("char", Integer.toString(c));
        }

        passInfo.addText(jsonObject.getString("passTail"));
        passInfo.addText(passInfo1);

        return passInfo;
    }

    public static String executeConfig(ClassInfo cInfo, String config, int index, String pass){
        PassInfo passInfo = new PassInfo(config);
        passInfo.setText("pass", pass);
        passInfo.setText("index", index);
        passInfo.setText("path", cInfo.path.replace("/", "\\\\"));
        return passInfo.text;
    }


}