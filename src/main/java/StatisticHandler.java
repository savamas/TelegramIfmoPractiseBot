import DTO.Discipline;
import DTO.Semester;
import DTO.User;
import DTO.Weather;
import net.sf.cglib.core.Local;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.IOException;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StatisticHandler {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static Collator myCollator = Collator.getInstance();

    public static String getStatistic(User user) throws IOException {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\User\\Downloads\\chromedriver_win32\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.get("https://de.ifmo.ru/?node=signin");
        WebElement loginElement = driver.findElement(By.name("LOGIN"));
        WebElement passwordElement = driver.findElement(By.name("PASSWD"));
        loginElement.sendKeys(user.getLogin());
        passwordElement.sendKeys(user.getPassword());
        loginElement.submit();
        try {
            WebElement ErrorElement = driver.findElement(By.id("d_dmessagetext"));
            return "Неверное имя пользователя или пароль!";
        } catch (Exception e) {
            /////
        }
        WebElement test = driver.findElement(By.partialLinkText("Электронный журнал"));
        driver.get(test.getAttribute("href"));

        WebElement bum = driver.findElement(By.xpath("//*[@id=\"d_s_m_content\"]/form[9]/div/table/tbody/tr[2]/td/select"));
        Select dropdown = new Select(bum);
        int amountOfYears = dropdown.getOptions().size();
        List<Semester> semesters = new ArrayList<>();
        for (int k = 0; k < amountOfYears; k++) {
            bum = driver.findElement(By.xpath("//*[@id=\"d_s_m_content\"]/form[9]/div/table/tbody/tr[2]/td/select"));
            dropdown = new Select(bum);
            dropdown.selectByIndex(k);
            bum = driver.findElement(By.xpath("//*[@id=\"d_s_m_content\"]/form[9]/div/table/tbody/tr[2]/td/select"));
            dropdown = new Select(bum);
            String semInterval = dropdown.getFirstSelectedOption().getText();

            List<WebElement> rows = driver.findElements(By.xpath(".//*[@id=\"FormName\"]/div/table/tbody/tr/td[1]"));
//            System.out.println("Amount of rows: " + rows.size());
            Semester currentSemester = null;
            List<Discipline> currentDisciplines = null;
            for (int i = 2; i < rows.size() + 4; i++) {
                WebElement elem = driver.findElement(By.xpath("//*[@id=\"FormName\"]/div/table/tbody/tr[" + i + "]"));
                if (myCollator.compare("Семестр", elem.getText().substring(0, 7)) == 0) {
                    if (i != 2) {
                        currentSemester.setDisciplines(currentDisciplines);
                        currentSemester.setStartDate(LocalDate.parse(semInterval.substring(0, 4) + "/09/01", dtf));
                        currentSemester.setFinishDate(LocalDate.parse(semInterval.substring(0, 4) + "/12/29", dtf));
                        semesters.add(currentSemester);
                    }
                    currentSemester = new Semester();
                    currentDisciplines = new ArrayList<>();
                    currentSemester.setSemesterIndex(Integer.parseInt(elem.getText().substring(7)));
                } else {
                    Discipline discipline = new Discipline();
                    WebElement tmp = driver.findElement(By.xpath("//*[@id=\"FormName\"]/div/table/tbody/tr[" + i + "]/td[3]"));
                    discipline.setSubject(tmp.getText());
                    tmp = driver.findElement(By.xpath("//*[@id=\"FormName\"]/div/table/tbody/tr[" + i + "]/td[4]"));
                    if ("".equals(tmp.getText())) {
                        discipline.setRating(0);
                    } else {
                        discipline.setRating(Double.parseDouble(tmp.getText().replace(',', '.')));
                    }
                    tmp = driver.findElement(By.xpath("//*[@id=\"FormName\"]/div/table/tbody/tr[" + i + "]/td[6]"));
                    discipline.setExam("".equals(tmp.getText()));
                    currentDisciplines.add(discipline);
                }
            }
            currentSemester.setDisciplines(currentDisciplines);
            semesters.add(currentSemester);
            currentSemester.setStartDate(LocalDate.parse(semInterval.substring(5) + "/02/05", dtf));
            currentSemester.setFinishDate(LocalDate.parse(semInterval.substring(5) + "/06/08", dtf));
        }

//        for (int i = 0; i < semesters.size(); i++) {
//            System.out.println("ID: " + semesters.get(i).getSemesterIndex());
//            System.out.println("StartDate = " + dtf.format(semesters.get(i).getStartDate()));
//            System.out.println("FinishDate = " + dtf.format(semesters.get(i).getFinishDate()));
//            for (int j = 0; j < semesters.get(i).getDisciplines().size(); j++) {
//                System.out.println("Name = " + semesters.get(i).getDisciplines().get(j).getSubject());
//                System.out.println("Rating = " + semesters.get(i).getDisciplines().get(j).getRating());
//                System.out.println("isExam = " + semesters.get(i).getDisciplines().get(j).isExam());
//            }
//        }

        LocalDate localDate = LocalDate.now();
     //   LocalDate localDate = LocalDate.parse("2019/06/07", dtf);

        int currentSemester = 0;
        for (int i = 0; i < semesters.size(); i++) {
            if (localDate.compareTo(semesters.get(semesters.size() - 1).getStartDate()) > 0) {
                currentSemester = semesters.size();
            } else {
                currentSemester = semesters.size() - 1;
            }
        }

        String debts = "У вас на текущий момент академические задолжности по следующим предметам: \n";
        int AmountOfDebts = 0;
        for (int i = 0; i < currentSemester - 1; i++) {
            for (int j = 0; j < semesters.get(i).getDisciplines().size(); j++) {
                if (semesters.get(i).getDisciplines().get(j).getRating() < 60) {
                    if (myCollator.compare("Физическая культура (элективная дисциплина) (Б1.В.17-УФКиС)", semesters.get(i).getDisciplines().get(j).getSubject()) != 0) {
                        AmountOfDebts++;
                        debts = debts + semesters.get(i).getDisciplines().get(j).getSubject() + ";\n";
                    }
                }
            }
        }

        String result = "";
        if (AmountOfDebts != 0) {
            if (AmountOfDebts >= 2) {
                result = debts + "Ситуация плачевна, советуем вам заполнить планк ПСЖ!\n\n";
            } else {
                result = debts + "Ещё не всё потеряно, следите за расписанием консультаций преподавателей!\n\n";
            }
        }

        result = result + "По этому семестру:\n ";
        Weather weather = new Weather();
        weather = WeatherHandler.getWeather("Saint Petersburg", weather);
        String firstModuleEnd;
        String secondModuleStart;
        String secondModuleFinish;
        if ((currentSemester & 1) == 0) {
            firstModuleEnd = dtf.format(localDate).substring(0, 4) + "/03/30";
            secondModuleStart = dtf.format(localDate).substring(0, 4) + "/05/15";
            secondModuleFinish = dtf.format(localDate).substring(0, 4) + "/06/08";
        } else {
            firstModuleEnd = dtf.format(localDate).substring(0, 4) + "/10/28";
            secondModuleStart = dtf.format(localDate).substring(0, 4) + "/11/25";
            secondModuleFinish = dtf.format(localDate).substring(0, 4) + "/12/29";
        }
        result = getCurrentSemesterStatistic(localDate, result, weather, semesters, currentSemester, firstModuleEnd, secondModuleStart, secondModuleFinish);
        System.out.println(result);

        return result;
    }

    public static String getLowMarkSubjects(List<Semester> semesters , int currentSemester, int bound) {
        String hint = "";
        for (int j = 0; j < semesters.get(currentSemester - 1).getDisciplines().size(); j++) {
            if (semesters.get(currentSemester - 1).getDisciplines().get(j).getRating() < bound) {
                if (myCollator.compare("Физическая культура (элективная дисциплина) (Б1.В.17-УФКиС)", semesters.get(currentSemester - 1).getDisciplines().get(j).getSubject()) != 0) {
                    hint = hint + semesters.get(currentSemester - 1).getDisciplines().get(j).getSubject() + " - " + semesters.get(currentSemester - 1).getDisciplines().get(j).getRating() + " баллов;\n";
                }
            }
        }
        return hint;
    }

    public static String getCurrentSemesterStatistic(LocalDate localDate, String resultStr, Weather weather, List<Semester> semesters, int currentSemester, String firstModuleEnd,
                                                                                                                                                            String secondModuleStart,
                                                                                                                                                            String secondModuleFinish) {
        String result = resultStr;
        if (localDate.compareTo(LocalDate.parse(firstModuleEnd, dtf)) < 0) {
            result = result + "Идёт ещё только первый модуль, многие преподаватели ещё не выставлили своих баллов, можете не волноваться!\n";
            if (weather.getTemp() > 0 && myCollator.compare("Clear", weather.getMain()) == 0) {
                result = result + "На улице чистое небо и " + weather.getTemp() + "°С градусов, советуем вам прогуляться по городу!";
            } else if (weather.getTemp() > 0 && myCollator.compare("Clouds", weather.getMain()) == 0) {
                result = result + "Хоть на улице и облачно, но " + weather.getTemp() + "°С градусов, советуем прогуляться до вуза!";
            } else if (weather.getTemp() <= 0 && myCollator.compare("Clouds", weather.getMain()) == 0) {
                result = result + "На улице облачно, ещё и " + weather.getTemp() + "°С градусов, советуем сидеть дома!";
            } else if (weather.getTemp() <= 0 && myCollator.compare("Clear", weather.getMain()) == 0) {
                result = result + "На улице чистое небо, однако " + weather.getTemp() + "°С градусов, в приниципе можете прогуляться!";
            }
        } else if (localDate.compareTo(LocalDate.parse(secondModuleStart, dtf)) < 0) {
            String hint = getLowMarkSubjects(semesters, currentSemester,30);
            if (hint == "") {
                result = result + "Отлично! У вас по каждому предмету набрано минимум 30 баллов! Держать в том же духе!\n";
                if (weather.getTemp() > 0 && myCollator.compare("Clear", weather.getMain()) == 0) {
                    result = result + "На улице чистое небо и " + weather.getTemp() + "°С градусов, советуем вам прогуляться по городу!";
                } else if (weather.getTemp() > 0 && myCollator.compare("Clouds", weather.getMain()) == 0) {
                    result = result + "Хоть на улице и облачно, но " + weather.getTemp() + "°С градусов, советуем прогуляться по городу!";
                } else if (weather.getTemp() <= 0 && myCollator.compare("Clouds", weather.getMain()) == 0) {
                    result = result + "На улице облачно, и вдобавок " + weather.getTemp() + "°С градусов, советуем сидеть дома!";
                } else if (weather.getTemp() <= 0 && myCollator.compare("Clear", weather.getMain()) == 0) {
                    result = result + "На улице чистое небо, однако " + weather.getTemp() + "°С градусов, в приниципе можете посетить вуз!";
                }
            } else {
                result = result + "Уже прошли все рубежки, однако баллы так и не появились, cоветуем обратить внимание на следующие предметы:\n " + hint;
                if (weather.getTemp() > 0 && myCollator.compare("Clear", weather.getMain()) == 0) {
                    result = result + "Несмотря на то что на улице чистое небо и " + weather.getTemp() + "°С градусов, советуем вам сходить в вуз!";
                } else if (weather.getTemp() > 0 && myCollator.compare("Clouds", weather.getMain()) == 0) {
                    result = result + "На улице облачно, и " + weather.getTemp() + "°С градусов, советуем вам посетить вуз!";
                } else if (weather.getTemp() <= 0 && myCollator.compare("Clouds", weather.getMain()) == 0) {
                    result = result + "На улице облачно, и " + weather.getTemp() + "°С градусов, но вам всё равно нужно идти в вуз!";
                } else if (weather.getTemp() <= 0 && myCollator.compare("Clear", weather.getMain()) == 0) {
                    result = result + "На улице чистое небо, однако " + weather.getTemp() + "°С градусов, лучше узнать что не так с баллами!";
                }
            }
        } else if (localDate.compareTo(LocalDate.parse(secondModuleFinish, dtf)) < 0) {
            String hint = getLowMarkSubjects(semesters, currentSemester, 60);
            if (hint == "") {
                result = result + "Великолепно! Вы закрыли сессию! По каждому предмету набрано минимум 60 баллов! Можете отдыхать и развеяться!\n";
                if (weather.getTemp() > 0 && myCollator.compare("Clear", weather.getMain()) == 0) {
                    result = result + "На улице чистое небо и " + weather.getTemp() + "°С градусов, советуем вам прогуляться по городу!";
                } else if (weather.getTemp() > 0 && myCollator.compare("Clouds", weather.getMain()) == 0) {
                    result = result + "Хоть на улице и облачно, но " + weather.getTemp() + "°С градусов, советуем прогуляться по городу!";
                }
            } else {
                result = result + "Скоро окончание семестра, однако баллов не хватает, cоветуем обратить внимание на следующие предметы:\n " + hint;
                if (weather.getTemp() > 0 && myCollator.compare("Clear", weather.getMain()) == 0) {
                    result = result + "Несмотря на то что на улице чистое небо и " + weather.getTemp() + "°С градусов, советуем вам добить все предметы и гулять спокойно!";
                } else if (weather.getTemp() > 0 && myCollator.compare("Clouds", weather.getMain()) == 0) {
                    result = result + "На улице облачно, и " + weather.getTemp() + "°С градусов, советуем вам набрать посетить ИТМО!";
                }
            }
        }
        return result;
    }
}
