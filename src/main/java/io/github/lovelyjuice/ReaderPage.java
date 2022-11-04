package io.github.lovelyjuice;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.util.List;

public class ReaderPage {

    public ReaderPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
    }


    @FindBy(css = "div.reader-chapter-content")
    public WebElement content;

    @FindBy(css = "button.nextChapter")
    public WebElement nextChapterMiddleButton;

    @FindBy(css = "head > link[rel='stylesheet']")
    public List<WebElement> styleSheetList;

    @FindBy(css = "title")
    public WebElement bookName;
}