package space.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Tools {

    // 删除目录 递归删除子目录
    public static boolean deleteDirectory(File dir, boolean deleteSelf) {
        // 目录不存在
        if (!dir.exists()) {
            return true;
        }
        // 目录不是目录
        if (!dir.isDirectory()) {
            return false;
        }
        // 获取目录下所有文件
        File[] files = dir.listFiles();
        // 目录下没有文件
        if (files == null) {
            return true;
        }
        // 遍历目录下所有文件
        for (File file : files) {
            // 是目录 递归删除子目录
            if (file.getPath().equals("SpacePacker\\libs")){
                continue;
            }
            if (file.isDirectory()) {
                deleteDirectory(file, true); // 递归删除子目录
            } else if (!file.delete()) {
                // 删除文件失败
                return false;
            }
        }
        // 删除目录失败
        if (deleteSelf) {
            return dir.delete();
        }
        return true;
    }


    // 保存为 UTF16-LE 文件
    public static void saveFile(String fileName, String text, Charset charset) throws IOException {
        Files.writeString(Paths.get(fileName), text, charset);
    }

    /**
     * 文本_取右边_从左往右查找
     * @param originalText 原始文本
     * @param textToFind 要查找的文本
     * @return 要查找的文本右边的文本
     */
    public static String getTextToRight(String originalText, String textToFind) {
        // 查找要查找的文本在原始文本中第一次出现的位置
        int index = originalText.indexOf(textToFind);
        // 如果找到了（index不等于-1）
        if (index != -1) {
            // 计算截取的起始位置：找到的位置 + 要查找文本的长度
            int startIndex = index + textToFind.length();
            // 从起始位置截取到字符串末尾
            return originalText.substring(startIndex);
        } else {
            // 如果没有找到，返回空字符串
            return "";
        }
    }

    /**
     * 文本_取出中间文本
     * @param originalText 原始文本
     * @param startText 开始文本
     * @param endText 结束文本
     * @return 中间文本
     */
    public static String getTextBetween(String originalText, String startText, String endText) {
        // 查找开始文本在原始文本中第一次出现的位置
        int startIndex = originalText.indexOf(startText);
        // 如果找到了（index不等于-1）
        if (startIndex != -1) {
            // 计算截取的起始位置：找到的位置 + 开始文本的长度
            int startIndex1 = startIndex + startText.length();
            // 查找结束文本在原始文本中第一次出现的位置
            int endIndex = originalText.indexOf(endText, startIndex1);
            // 如果找到了（index不等于-1）
            if (endIndex != -1) {
                // 从起始位置截取到结束位置
                return originalText.substring(startIndex1, endIndex);
            } else {
                // 如果没有找到结束文本，返回空字符串
                return "";
            }
        } else {
            // 如果没有找到开始文本，返回空字符串
            return "";
        }
    }

//    // 文本出现次数
//    public static int countOccurrences(String text, char pattern) {
//        int count = 0;
//        for (char c : text.toCharArray()) {
//            if (c == pattern) {
//                count++;
//            }
//        }
//        return count;
//    }

}
