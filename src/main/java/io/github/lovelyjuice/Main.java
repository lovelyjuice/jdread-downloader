package io.github.lovelyjuice;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        ChromeDriver driver = new ChromeDriver();
        driver.get("https://passport.jd.com/new/login.aspx?ReturnUrl=https%3A%2F%2Febooks.jd.com%2Fbookshelf");
        System.out.println("操作浏览器登录并跳转到书籍第一页后，输入任意字符开始，输入q退出：");
        while (!(new Scanner(System.in).next()).equals("q")) {
            try {
                var contentBuilder = new StringBuilder("<body>");
                var styleSet = new LinkedHashSet<String>();
                var readerPage = new ReaderPage(driver);
                var bookName = readerPage.bookName.getAttribute("innerText");
                System.out.printf("开始爬取《%s》%n", bookName);
                int debugTimes = 0;
                String debugFlag = System.getenv("debug");
                while (true) {
                    if (debugTimes++ > 6 && debugFlag != null) break;  //调试的时候只爬取6章
                    String contentHtml = readerPage.content.getAttribute("outerHTML");
                    System.out.println(contentHtml.split("reader-chapter-content")[1].substring(0, 50));
                    contentBuilder.append(contentHtml);
                    styleSet.addAll(readerPage.styleSheetList.stream()  //有些书不同章节有不同的css样式，所以试着合并这些css
                            .map(webElement -> webElement.getAttribute("outerHTML"))
                            .collect(Collectors.toList()));
                    System.out.println("-----------------------");
                    try {
                        readerPage.nextChapterMiddleButton.click();
                    } catch (NoSuchElementException e) {
                        break;  //找不到“下一章”按钮说明已经浏览到最后一章
                    }
                    new WebDriverWait(driver, Duration.ofSeconds(300))
                            .until(a -> a.findElement(By.cssSelector("div.reader-chapter-content")));   //“下一章”按钮在正文之前被渲染出来，所以只要渲染出按钮就可以跳转到下一章
                }
                contentBuilder.append("</body></html>");
                bookName = bookName.replace(":", "：").replace("?", "？").replace("\"", "“")
                        .replaceAll("[\\\\\\/\\*<>\\|]", "_");   //防止书名中存在Windows不允许的文件名字符
                PrintWriter writer = new PrintWriter(bookName + ".html");
                String staticStylesheet = "<style>.reader-chapter-content>* {" +
                        "    word-wrap: break-all;" +
                        "    margin-top: 18px;" +
                        "    text-align: justify;" +
                        "    word-break: break-word;" +
                        "}</style>";    // main.css中阅读区域的默认样式
                writer.write("<html><head>" + staticStylesheet + String.join("\n", styleSet) + "</head>");
                String content = contentBuilder.toString().replace("min-height: ;", "")
                        .replace("; height: \"", ";\"")      //京东前端写的css缺少属性值，不删掉的话转换epub时会报错，不过其实报错问题也不大
                        .replaceAll("min-width:(.*?);", "");  //解除图片最小宽度限制，小屏设备也能轻松查看，但是对于百分比宽度的图片无效
                writer.write(content);
                writer.close();
                System.out.printf("《%s》下载完成！%n", bookName);
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("出错了，请重试！");
            }
            System.out.println("跳转到书籍第一页后，输入任意字符开始，输入q退出：");
        }
        driver.quit();
    }
}