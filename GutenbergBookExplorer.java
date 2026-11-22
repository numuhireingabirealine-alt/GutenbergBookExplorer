
package com.student.gutenbergbookexplorer;

import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GutenbergBookExplorer {

    static final int MAX_BOOKS = 100;
    static final int MAX_READING = 20;

    static String[] bookTitles = new String[MAX_BOOKS];
    static String[] bookAuthors = new String[MAX_BOOKS];
    static String[] bookLinks = new String[MAX_BOOKS];
    static String[] bookPrices = new String[MAX_BOOKS];
    static String[] bookRatings = new String[MAX_BOOKS];
    static String[] bookAvailability = new String[MAX_BOOKS];

    static int bookCount = 0;

    static String[] readingTitles = new String[MAX_READING];
    static String[] readingAuthors = new String[MAX_READING];
    static String[] readingLinks = new String[MAX_READING];
    static String[] readingPrices = new String[MAX_READING];
    static String[] readingRatings = new String[MAX_READING];
    static String[] readingAvailability = new String[MAX_READING];

    static int readingCount = 0;

    static final String AUTHOR_NOT_AVAILABLE =
            "Not provided by Books to Scrape";

    static final String BASE_URL =
            "https://books.toscrape.com/catalogue/page-1.html";

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n====================================");
            System.out.println("       GUTENBERG BOOK EXPLORER");
            System.out.println("====================================");
            System.out.println("1. Explore Books");
            System.out.println("2. Search Books by Title");
            System.out.println("3. Search Books by Author");
            System.out.println("4. Add Book to Reading List");
            System.out.println("5. View Reading List");
            System.out.println("6. Remove a Book");
            System.out.println("7. Display Book Details");
            System.out.println("0. Exit");
            System.out.println("------------------------------------");
            System.out.print("Enter your choice: ");

            while (!input.hasNextInt()) {
                System.out.println("Invalid input. Enter a number.");
                input.next();
                System.out.print("Enter your choice: ");
            }

            choice = input.nextInt();
            input.nextLine();

            switch (choice) {

                case 1:
                    exploreBooks();
                    break;

                case 2:
                    System.out.print("Enter book title: ");
                    String title = input.nextLine().trim();

                    if (!title.isEmpty()) {
                        searchBook(title);
                    } else {
                        System.out.println("Title cannot be empty.");
                    }
                    break;

                case 3:
                    System.out.print("Enter author name: ");
                    String author = input.nextLine().trim();

                    if (!author.isEmpty()) {
                        searchBook(author, true);
                    } else {
                        System.out.println("Author cannot be empty.");
                    }
                    break;

                case 4:
                    addBook(input);
                    break;

                case 5:
                    viewReadingList();
                    break;

                case 6:
                    removeBook(input);
                    break;

                case 7:
                    displayBookDetails(input);
                    break;

                case 0:
                    System.out.println("Thank you for using Gutenberg!");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }

        } while (choice != 0);

        input.close();
    }

    public static void exploreBooks() {

        System.out.println("\n===== EXPLORE BOOKS =====");
        System.out.println("Source: Books to Scrape");

        bookCount = 0;
        loadBooks("", true);
        displayBooks();
    }

    public static void searchBook(String title) {

        System.out.println("\n===== SEARCH BOOKS BY TITLE =====");

        bookCount = 0;
        loadBooks(title, false);
        displayBooks();
    }

    public static void searchBook(String keyword,
                                  boolean searchByAuthor) {

        if (searchByAuthor) {
            System.out.println("\n===== SEARCH BOOKS BY AUTHOR =====");
            System.out.println("Author search cannot be completed because");
            System.out.println("Books to Scrape does not provide author names.");
            System.out.println("The program does not invent author information.");
        } else {
            searchBook(keyword);
        }
    }

    public static void loadBooks(String keyword, boolean explore) {

        String currentUrl = BASE_URL;
        int pageNumber = 1;

        try {
            while (currentUrl != null && pageNumber <= 5) {

                Document document = Jsoup.connect(currentUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(7000)
                        .get();

                Elements books = document.select("article.product_pod");

                for (Element book : books) {

                    Element titleElement = book.selectFirst("h3 a");

                    if (titleElement == null) {
                        continue;
                    }

                    String title = titleElement.attr("title");
                    String link = titleElement.attr("abs:href");

                    Element priceElement = book.selectFirst(".price_color");
                    Element ratingElement = book.selectFirst(".star-rating");
                    Element availabilityElement =
                            book.selectFirst(".availability");

                    String price = "Not available";
                    String rating = "Not available";
                    String availability = "Not available";

                    if (priceElement != null) {
                        price = cleanPrice(priceElement.text());
                    }

                    if (ratingElement != null) {
                        rating = getRating(ratingElement);
                    }

                    if (availabilityElement != null) {
                        availability = availabilityElement.text().trim();
                    }

                    if (explore || title.toLowerCase().contains(
                            keyword.toLowerCase())) {

                        if (bookCount < MAX_BOOKS) {
                            bookTitles[bookCount] = title;
                            bookAuthors[bookCount] = AUTHOR_NOT_AVAILABLE;
                            bookLinks[bookCount] = link;
                            bookPrices[bookCount] = price;
                            bookRatings[bookCount] = rating;
                            bookAvailability[bookCount] = availability;
                            bookCount++;
                        }
                    }
                }

                if (explore || bookCount > 0) {
                    break;
                }

                Element nextPage = document.selectFirst("li.next a");

                if (nextPage == null) {
                    break;
                }

                currentUrl = nextPage.attr("abs:href");
                pageNumber++;
            }

        } catch (Exception e) {
            if (bookCount == 0) {
                System.out.println("Could not connect to Books to Scrape.");
                System.out.println("Please check your internet connection and try again.");
            } else {
                System.out.println("Some pages could not be loaded,");
                System.out.println("but the matching books found are displayed below.");
            }
        }
    }

    public static String cleanPrice(String price) {

        if (price == null || price.isEmpty()) {
            return "Not available";
        }

        return price.replace("£", "").trim();
    }

    public static String formatPrice(String price) {

        if (price == null || price.equals("Not available")) {
            return "Not available";
        }

        return "GBP " + cleanPrice(price);
    }

    public static String getRating(Element ratingElement) {

        if (ratingElement == null) {
            return "Not available";
        }

        if (ratingElement.hasClass("One")) {
            return "1 Star";
        }

        if (ratingElement.hasClass("Two")) {
            return "2 Stars";
        }

        if (ratingElement.hasClass("Three")) {
            return "3 Stars";
        }

        if (ratingElement.hasClass("Four")) {
            return "4 Stars";
        }

        if (ratingElement.hasClass("Five")) {
            return "5 Stars";
        }

        return "Not available";
    }

    public static void displayBooks() {

        if (bookCount == 0) {
            System.out.println("No books found.");
            return;
        }

        System.out.println("\nTotal books found: " + bookCount);

        for (int i = 0; i < bookCount; i++) {
            System.out.println("\nBook Number: " + (i + 1));
            System.out.println("Title: " + bookTitles[i]);
            System.out.println("Author: " + bookAuthors[i]);
            System.out.println("Price: " + formatPrice(bookPrices[i]));
            System.out.println("Rating: " + bookRatings[i]);
            System.out.println("Availability: " + bookAvailability[i]);
            System.out.println("Book URL: " + bookLinks[i]);
        }
    }

    public static void addBook(Scanner input) {

        if (bookCount == 0) {
            System.out.println("Please explore or search books first.");
            return;
        }

        if (readingCount >= MAX_READING) {
            System.out.println("Your Reading List is full.");
            return;
        }

        displayBooks();

        System.out.print("\nEnter book number to add: ");

        if (!input.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            input.nextLine();
            return;
        }

        int number = input.nextInt();
        input.nextLine();

        if (number < 1 || number > bookCount) {
            System.out.println("Invalid book number.");
            return;
        }

        int index = number - 1;

        for (int i = 0; i < readingCount; i++) {
            if (readingLinks[i].equals(bookLinks[index])) {
                System.out.println("This book is already saved.");
                return;
            }
        }

        readingTitles[readingCount] = bookTitles[index];
        readingAuthors[readingCount] = bookAuthors[index];
        readingLinks[readingCount] = bookLinks[index];
        readingPrices[readingCount] = bookPrices[index];
        readingRatings[readingCount] = bookRatings[index];
        readingAvailability[readingCount] = bookAvailability[index];

        readingCount++;

        System.out.println("Book added successfully!");
    }

    public static void viewReadingList() {

        System.out.println("\n===== MY READING LIST =====");

        if (readingCount == 0) {
            System.out.println("Your Reading List is empty.");
            return;
        }

        for (int i = 0; i < readingCount; i++) {
            System.out.println("\nBook Number: " + (i + 1));
            System.out.println("Title: " + readingTitles[i]);
            System.out.println("Author: " + readingAuthors[i]);
            System.out.println("Price: " + formatPrice(readingPrices[i]));
            System.out.println("Rating: " + readingRatings[i]);
            System.out.println("Availability: " + readingAvailability[i]);
            System.out.println("Book URL: " + readingLinks[i]);
        }

        System.out.println("\nTotal saved books: " + readingCount);
    }

    public static void removeBook(Scanner input) {

        if (readingCount == 0) {
            System.out.println("Your Reading List is empty.");
            return;
        }

        viewReadingList();

        System.out.print("\nEnter book number to remove: ");

        if (!input.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            input.nextLine();
            return;
        }

        int number = input.nextInt();
        input.nextLine();

        if (number < 1 || number > readingCount) {
            System.out.println("Invalid book number.");
            return;
        }

        int index = number - 1;

        for (int i = index; i < readingCount - 1; i++) {
            readingTitles[i] = readingTitles[i + 1];
            readingAuthors[i] = readingAuthors[i + 1];
            readingLinks[i] = readingLinks[i + 1];
            readingPrices[i] = readingPrices[i + 1];
            readingRatings[i] = readingRatings[i + 1];
            readingAvailability[i] = readingAvailability[i + 1];
        }

        readingCount--;

        readingTitles[readingCount] = null;
        readingAuthors[readingCount] = null;
        readingLinks[readingCount] = null;
        readingPrices[readingCount] = null;
        readingRatings[readingCount] = null;
        readingAvailability[readingCount] = null;

        System.out.println("Book removed successfully!");
    }

    public static void displayBookDetails(Scanner input) {

        if (readingCount == 0) {
            System.out.println("Your Reading List is empty.");
            return;
        }

        viewReadingList();

        System.out.print("\nEnter book number: ");

        if (!input.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            input.nextLine();
            return;
        }

        int number = input.nextInt();
        input.nextLine();

        if (number < 1 || number > readingCount) {
            System.out.println("Invalid book number.");
            return;
        }

        int index = number - 1;

        System.out.println("\n===== BOOK DETAILS =====");
        System.out.println("Title: " + readingTitles[index]);
        System.out.println("Author: " + readingAuthors[index]);
        System.out.println("Price: " + formatPrice(readingPrices[index]));
        System.out.println("Rating: " + readingRatings[index]);
        System.out.println("Availability: " + readingAvailability[index]);
        System.out.println("Book URL: " + readingLinks[index]);
    }
}
