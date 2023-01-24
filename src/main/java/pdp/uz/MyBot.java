package pdp.uz;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pdp.uz.model.BotStep;
import pdp.uz.model.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class MyBot extends TelegramLongPollingBot {

    List<User> users = new ArrayList<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()){
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();
            User user = getSaveUser(chatId);
            if (message.hasText()){
                String text = message.getText();
                if (text.equals("/list")){
                    System.out.println(users);
                }
                if (text.equals("/start")){
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("Assalom aleykum" +
                            "\nBotga Xush Kelibsiz");
                    sendMessage.setChatId(chatId);

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    sendMessage.setReplyMarkup(replyKeyboardMarkup);
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    List<KeyboardRow> keyboardRowList = new ArrayList<>();

                    KeyboardRow row1 = new KeyboardRow();
                    row1.add(new KeyboardButton("Generate QR code"));
                    row1.add(new KeyboardButton("Read QR code"));
                    keyboardRowList.add(row1);
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);

                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }

                    user.setStep(BotStep.SELECT_BUTTON);
                }
                else if (user.getStep().equals(BotStep.SELECT_BUTTON) && text.equals("Generate QR code")){
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("Enter Text:");
                    sendMessage.setChatId(chatId);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    user.setStep(BotStep.CONVERT_TEXT_TO_QR);
                }
                else if (user.getStep().equals(BotStep.CONVERT_TEXT_TO_QR)){
//                    SendMessage sendMessage = new SendMessage();
                    user.setText(text);
                    String qrCodeText = text;
                    String filePath = "JD.png";
                    int size = 125;
                    String fileType = "png";
                    File qrFile = new File("src/main/resources/qr/" + filePath);
                    try {
                        createQRImage(qrFile, qrCodeText, size, fileType);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (WriterException e) {
                        throw new RuntimeException(e);
                    }

                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setPhoto(new InputFile(qrFile));
                    sendPhoto.setChatId(chatId);

//                    System.out.println("DONE");

                    try {
                        execute(sendPhoto);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }

                    user.setStep(BotStep.SELECT_BUTTON);
                }

                else if (user.getStep().equals(BotStep.SELECT_BUTTON) && text.equals("Read QR code")){
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("Send QR Photo:");
                    sendMessage.setChatId(chatId);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    user.setStep(BotStep.CONVERT_PHOTO_TO_TEXT);
                }
                else if (user.getStep().equals(BotStep.SELECT_BUTTON)){
                    SendMessage sendMessage = new SendMessage();

                    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                    sendMessage.setReplyMarkup(replyKeyboardMarkup);
                    replyKeyboardMarkup.setSelective(true);
                    replyKeyboardMarkup.setResizeKeyboard(true);
                    replyKeyboardMarkup.setOneTimeKeyboard(true);

                    List<KeyboardRow> keyboardRowList = new ArrayList<>();

                    KeyboardRow row1 = new KeyboardRow();
                    row1.add(new KeyboardButton("Generate QR code"));
                    row1.add(new KeyboardButton("Read QR code"));
                    keyboardRowList.add(row1);
                    replyKeyboardMarkup.setKeyboard(keyboardRowList);
                    sendMessage.setChatId(chatId);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }


                }
            else if (message.hasPhoto()) {
                if (user.getStep().equals(BotStep.CONVERT_PHOTO_TO_TEXT)){
                    SendMessage sendMessage = new SendMessage();
                    List<PhotoSize> photo = message.getPhoto();
                    for (PhotoSize photoSize : message.getPhoto()) {
                        saveFileToFolder(photoSize.getFileId(), "shift.png");
                    }

                    // ******************************************************************************************
                    // ******************************************************************************************

                    File fileobj = new File("src/main/resources/comingPhoto/shift.png");

                    BufferedImage bfrdImgobj = null;
                    try {
                        bfrdImgobj = ImageIO.read(fileobj);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    LuminanceSource source = new BufferedImageLuminanceSource(bfrdImgobj);
                    BinaryBitmap binarybitmapobj = new BinaryBitmap(new HybridBinarizer(source));

                    Result resultobj = null;
                    try {
                        resultobj = new MultiFormatReader().decode(binarybitmapobj);
                    } catch (NotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    sendMessage.setText(resultobj.getText());
                    sendMessage.setChatId(chatId);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }

//                    System.out.println("Data Stored In our QR Code" +"  " + resultobj.getText());

                }


                    user.setStep(BotStep.SELECT_BUTTON);
                }
            }
        }





    private void createQRImage(File qrFile, String qrCodeText, int size, String fileType) throws IOException, WriterException {
        // Create the ByteMatrix for the QR-Code that encodes the given String
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
        // Make the BufferedImage that are to hold the QRCode
        int matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // Paint and save the image using the ByteMatrix
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        ImageIO.write(image, fileType, qrFile);
    }

    private void saveFileToFolder(String fileId, String fileName) {
//        GetFile getFile = new GetFile(document.getFileId());
        GetFile getFile = new GetFile(fileId);
        try {
            org.telegram.telegrambots.meta.api.objects.File tgFile = execute(getFile);
            String fileUrl = tgFile.getFileUrl(getBotToken());
            URL url = new URL(fileUrl);
            InputStream inputStream = url.openStream();
//            FileUtils.copyInputStreamToFile(inputStream, new java.io.File("src/main/resources/files/" + document.getFileName()));
            FileUtils.copyInputStreamToFile(inputStream, new java.io.File("src/main/resources/comingPhoto/" + fileName));


        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private User getSaveUser(String chatId) {
        for (User user : users) {
            if (user.getChatId().equals(chatId)){
                return user;
            }
        }
        User user = new User();
        user.setChatId(chatId);
        users.add(user);
        return user;
    }


    @Override
    public String getBotToken() {
        return "-----";
    }
    @Override
    public String getBotUsername() {
        return "-------";
    }
}
