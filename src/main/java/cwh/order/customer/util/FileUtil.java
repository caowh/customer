package cwh.order.customer.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Calendar;

/**
 * Created by 曹文豪 on 2018/10/30 0030.
 */
public class FileUtil {

    public static String save(MultipartFile file) throws IOException {
        Calendar cal = Calendar.getInstance();
        String path = cal.get(Calendar.YEAR)
                + File.separator + cal.get(Calendar.MONTH) + File.separator + cal.get(Calendar.DATE);
        String name = file.getOriginalFilename();
        String fileName = System.currentTimeMillis() + name.substring(name.lastIndexOf("."), name.length());
        FileUtil.saveFile(file.getInputStream(), fileName, Constant.FILEPATH + File.separator + path);
        return path + File.separator + fileName;
    }

    public static Boolean delete(String url) {
        String path = Constant.FILEPATH + File.separator + url;
        File file = new File(path);
        return file.delete();
    }

    private static void saveFile(InputStream inputStream, String fileName, String path) {
        OutputStream os = null;
        try {
            // 1K的数据缓冲
            byte[] bs = new byte[1024];
            // 读取到的数据长度
            int len;
            // 输出的文件流保存到本地文件
            File tempFile = new File(path);
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            os = new FileOutputStream(tempFile.getPath() + File.separator + fileName);
            // 开始读取
            while ((len = inputStream.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 完毕，关闭所有链接
            try {
                if (os != null) {
                    os.close();
                }
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
