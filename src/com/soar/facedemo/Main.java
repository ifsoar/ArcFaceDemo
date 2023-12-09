package com.soar.facedemo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.TypeReference;
import com.arcsoft.face.*;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectOrient;
import com.arcsoft.face.enums.ErrorInfo;
import com.arcsoft.face.toolkit.ImageFactory;
import com.arcsoft.face.toolkit.ImageInfo;
import com.soar.facedemo.model.T_Entry;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        if (true) {
            twitterTest();
            return;
        }
        //从官网获取
        String appId = "Ggxg2b1XBGjjdNzUU5A6pVviMZKoHmmDZ4Xwj5kUogub";
        String sdkKey = "9CDMbma2ptwXJvYNDuY3DVLqSLy57htL5fCjv5WD5t1a";
        File dir = new File("C:\\Users\\soar\\Desktop\\face");

        FaceEngine faceEngine = new FaceEngine("E:\\Space\\Intellij\\ArcFaceDemo\\libs\\WIN64");
        //激活引擎
        int errorCode = faceEngine.activeOnline(appId, sdkKey);

        if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            System.out.println("引擎激活失败");
        }


        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
        errorCode = faceEngine.getActiveFileInfo(activeFileInfo);
        if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            System.out.println("获取激活文件信息失败");
        }


        //引擎配置
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_VIDEO);//识别模式：静态图片识别
        engineConfiguration.setDetectFaceOrientPriority(DetectOrient.ASF_OP_ALL_OUT);//检测角度优先级：全角度检测
        engineConfiguration.setDetectFaceMaxNum(16);//帧内最多人脸数量
        engineConfiguration.setDetectFaceScaleVal(32);//最小人脸比例：图片长边/人脸框长边的比值，video模式推荐16，image模式推荐32
        //功能配置
        FunctionConfiguration functionConfiguration = new FunctionConfiguration();
        functionConfiguration.setSupportFaceDetect(true);//是否支持人脸检测功能
        functionConfiguration.setSupportFaceRecognition(true);//是否支持人脸识别功能
        functionConfiguration.setSupportAge(true);//是否支持年龄检测功能
        functionConfiguration.setSupportGender(true);//是否支持性别检测功能
        functionConfiguration.setSupportFace3dAngle(false);//是否支持3D检测功能
        functionConfiguration.setSupportLiveness(false);//是否支持RGB活体检测功能
        functionConfiguration.setSupportIRLiveness(false);//是否支持IR活体检测功能
        engineConfiguration.setFunctionConfiguration(functionConfiguration);


        //初始化引擎
        errorCode = faceEngine.init(engineConfiguration);

        if (errorCode != ErrorInfo.MOK.getValue()) {
            System.out.println("初始化引擎失败");
        }
        File imgDir = new File(dir, "frameImg");
        final int size = imgDir.listFiles().length;
        String nameTemp = "image-%04d.jpg";
        List<FaceInfo> faceInfoList = new ArrayList<FaceInfo>();
        for (int i = 1; i <= size; i++) {
            File img = new File(imgDir, String.format(nameTemp, i));
            //原始图片
//            File originFile = new File("C:\\Users\\soar\\Desktop\\微信截图_20230305164224.png");
            ImageInfo imageInfo = ImageFactory.getRGBData(img);
            faceInfoList.clear();
            errorCode = faceEngine.detectFaces(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList);
            if (errorCode != ErrorInfo.MOK.getValue()) {
                System.out.println("人脸检测失败");
            }

            System.out.println("人脸数量：" + faceInfoList.size());

            if (faceInfoList.size() > 0) {
                BufferedImage bufferedImage = ImageIO.read(img);
                Graphics graphics = bufferedImage.getGraphics();
                graphics.setColor(Color.red);
                int x, y, w, h;
                for (FaceInfo info : faceInfoList) {
                    System.out.println(info.getFaceId() + "\t" + info.getRect().left + " " + info.getRect().top + " " + info.getRect().right + " " + info.getRect().bottom);

                    x = info.getRect().left;
                    y = info.getRect().top;
                    w = info.getRect().right - info.getRect().left;
                    h = info.getRect().bottom - info.getRect().top;
                    graphics.drawRect(x, y, w, h);
                }
                File outputFile = new File(imgDir, img.getName() + "_out.png");
                ImageIO.write(bufferedImage, "png", outputFile);
            }
        }

    }

    //推特api测试
    private static void twitterTest() throws Exception {
        Robot robot = new Robot();
        robot.delay(5000);
        int x = (int) (1330 / 1.25F);
        int y = (int) (455 / 1.25F);
        //3290,181
        robot.mouseMove(x, y);
        robot.delay(2000);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.delay(20);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
//        parseOrigin();
//        Thread.sleep(1000);
//        parseEntries();
    }

    private static void parseEntries() throws Exception {
        //从entries数组解析成java对象数组
        File file = new File("entries.json");
        FileReader fileReader = new FileReader(file, Charset.defaultCharset());
        JSONReader reader = new JSONReader(fileReader);
        T_Entry[] entries = reader.readObject(new TypeReference<T_Entry[]>() {
        });
        List<T_Entry> entryList = new ArrayList<>();
        long top = -1, bottom = -1;
        for (T_Entry entry : entries) {
            if (entry.content.entryType.equals("TimelineTimelineItem")) {
                entryList.add(entry);
            } else if (entry.entryId.startsWith("cursor-top-")) {
                top = Long.valueOf(entry.sortIndex);
            } else if (entry.entryId.startsWith("cursor-bottom-")) {
                bottom = Long.valueOf(entry.sortIndex);
            }
        }
        if (top == -1 || bottom == -1) {
            System.err.println("查不到top和bottom");
            return;
        }

        String name = String.format("%d_%d_%d.json", bottom + 1, top - 1, top - bottom - 1);
        File out = new File(name);
        FileWriter writer = new FileWriter(out, Charset.defaultCharset());
        writer.write(JSONArray.toJSONString(entryList));
        writer.flush();
        writer.close();
    }

    private static void parseOrigin() throws Exception {
        //从原始json解析出entries数组
        File file = new File("origin.json");
        FileReader fileReader = new FileReader(file, Charset.defaultCharset());
        JSONReader reader = new JSONReader(fileReader);
        JSONObject object = (JSONObject) reader.readObject();
        //data.user.result.timeline_v2.timeline.instructions[0].entries[0]
        JSONArray entries = object
                .getJSONObject("data")
                .getJSONObject("user")
                .getJSONObject("result")
                .getJSONObject("timeline_v2")
                .getJSONObject("timeline")
                .getJSONArray("instructions").getJSONObject(0)
                .getJSONArray("entries");
        File out = new File("entries.json");
        FileWriter writer = new FileWriter(out, Charset.defaultCharset());
        writer.write(entries.toJSONString());
        writer.flush();
        writer.close();

    }
}
