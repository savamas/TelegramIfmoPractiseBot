import DTO.User;
import DTO.Weather;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SharagaBot extends TelegramLongPollingBot {
    private BotState currentBotState = BotState.None;
    private User user = new User();

    public static void main(String[] args) throws IOException {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new SharagaBot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }


    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            switch (currentBotState) {
                case None:
                    switch (message.getText()) {
                        case "/start":
                            sendMsg(message, "Вас приветствует бот-ассистент!" + StickersCollection.GREETING + StickersCollection.MONKEY + "\n" +
                                    "В его компетенции - оценивание текущего состояния по баллам и задолжностям с учётом даты и погоды и последующим предложением, связанным с посещением НИУ ИТМО\n" +
                                    "Вся информация берётся с сайта de.ifmo.ru\n" +
                                    "Желаем удачной работы!" + StickersCollection.DEVIL);
                            break;
                        case "/help":
                            sendMsg(message, "Спаси и сохрани!");
                            break;
                        case "/authorization":
                            currentBotState = BotState.AuthorizationLogin;
                            sendMsg(message, "Введите логин:");
                            break;
                        default:
                            sendMsg(message, "Неверная команда!");
                    }
                    break;
                case AuthorizationLogin:
                    currentBotState = BotState.AuthorizationPassword;
                    user.setLogin(message.getText());
                    sendMsg(message, "Введите пароль:");
                    break;
                case AuthorizationPassword:
                    sendMsg(message, "Подключение...");
                    currentBotState = BotState.None;
                    user.setPassword(message.getText());
                    String result = null;
                    try {
                        result = StatisticHandler.getStatistic(user);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if ("Неверное имя пользователя или пароль!".equals(result)) {
                        sendMsg(message, result);
                    } else {
                        sendMsg(message,"Авторизация прошла успешна!\n\n" + result);
                    }
                    break;
                case InformationProcess:
                    sendMsg(message, "Идёт обработка информации!");
                    break;
            }
        }
    }

    public void sendMsg(Message message, String text) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
  //      sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        try {
            setButtons(sendMessage);
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void setButtons(SendMessage sendMessage){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        if (currentBotState == BotState.None) {
            keyboardFirstRow.add(new KeyboardButton("/help"));
            keyboardFirstRow.add(new KeyboardButton("/authorization"));
            keyboardRowList.add(keyboardFirstRow);
        } else {
            sendMessage.setReplyMarkup(new ReplyKeyboardRemove());
        }
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    public String getBotUsername() {
        return "ifmo_assistent_bot";
    }

    public String getBotToken() {
        return "768395064:AAF7qXZaRU3mPd1w8SjLgN63ugfxZrEup7I";
    }
}
