package space;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Time;
import java.util.List;

public class FileTransferHandler extends TransferHandler {

    private final JLabel label; // 拖拽标签
    private final JLabel label1; // 结束倒计时标签

    public FileTransferHandler(JLabel label, JLabel label1) {
        this.label = label;
        this.label1 = label1;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        // 只支持文件拖拽
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public boolean importData(TransferSupport support) {

        Object fileType;
        try {
            // 从拖拽数据中获取文件列表
            fileType = support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // 检查是否为文件列表
        if (!(fileType instanceof List<?> fileObjList)) {
            // 不是文件列表，直接返回
            label.setText("这可能不是需要处理的文件！");
            return false;
        }

        // 获取文件列表中的所有文件
        File file = null;
        for (Object o : fileObjList) {
            if (o instanceof File fileItem) {
                file = fileItem;
                break;
            }
        }

        // 检查是否有文件
        if (file == null) {
            label.setText("没有找到需要处理的文件！");
            return false;
        }

        // 处理文件
        try {
            JavaDllPacker.dispose(file.getPath(), this.label);
            label.setText("完成！");
            startCountdownTimer();
            return true;
        } catch (Exception e) {
            label.setText(e.getMessage());
            return false;
        }

    }

    private void startCountdownTimer() {
        // 创建计时器，每秒触发一次
        Timer timer = new Timer(1000, new CountdownAction());
        timer.setInitialDelay(0); // 立即开始
        timer.start();
    }

    private class CountdownAction implements ActionListener {
        private int count = 3;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (count > 0) {
                label1.setText(String.valueOf(count));
                count--;
            } else if (e.getSource() instanceof Timer time){
                // 倒计时结束，停止计时器并退出
                time.stop();
                System.exit(0);
            }
        }
    }

}
