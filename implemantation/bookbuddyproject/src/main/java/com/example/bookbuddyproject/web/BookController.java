package com.example.bookbuddyproject.web;

import com.example.bookbuddyproject.domain.Book;
import com.example.bookbuddyproject.domain.BookCondition;
import com.example.bookbuddyproject.domain.Member;
import com.example.bookbuddyproject.service.BookService;
import com.example.bookbuddyproject.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final WishlistService wishlistService;

    /**
     * 도서 목록 (검색)
     */
    @GetMapping
    // 수정: name 속성 추가
    public String list(@RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "category", required = false) String category,
                       @RequestParam(name = "minPrice", required = false) Integer minPrice,
                       @RequestParam(name = "maxPrice", required = false) Integer maxPrice,
                       @RequestParam(name = "bookCondition", required = false) BookCondition bookCondition,
                       Model model) {

        List<Book> books;
        if (keyword != null || category != null || minPrice != null || maxPrice != null || bookCondition != null) {
            books = bookService.searchBooks(keyword, category, minPrice, maxPrice, bookCondition);
        } else {
            books = bookService.findBooksOnSale();
        }

        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("bookCondition", bookCondition);
        model.addAttribute("bookConditions", BookCondition.values());

        return "books/bookList";
    }

    /**
     * 도서 등록 폼
     */
    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        model.addAttribute("bookForm", new BookForm());
        model.addAttribute("bookConditions", BookCondition.values());
        return "books/createBookForm";
    }

    /**
     * 도서 등록 처리
     */
    @PostMapping("/new")
    public String create(@Valid BookForm form, BindingResult result,
                         HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("bookConditions", BookCondition.values());
            return "books/createBookForm";
        }

        bookService.registerBook(
                loginMember.getId(),
                form.getTitle(),
                form.getAuthor(),
                form.getPublisher(),
                form.getPrice(),
                form.getCategory(),
                form.getBookCondition(),
                form.getDescription(),
                form.getImageUrl()
        );

        return "redirect:/books/my";
    }

    /**
     * 도서 상세 조회
     */
    @GetMapping("/{bookId}")
    // 수정 name="bookId" 추가
    public String detail(@PathVariable(name = "bookId") Long bookId, HttpSession session, Model model) {
        Book book = bookService.viewBook(bookId);  // 조회수 증가
        model.addAttribute("book", book);

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember != null) {
            boolean isWished = wishlistService.isWished(loginMember.getId(), bookId);
            boolean isOwner = book.getSeller().getId().equals(loginMember.getId());
            model.addAttribute("isWished", isWished);
            model.addAttribute("isOwner", isOwner);
        }

        long wishCount = wishlistService.getWishCount(bookId);
        model.addAttribute("wishCount", wishCount);

        return "books/bookDetail";
    }

    /**
     * 내 판매 도서 목록
     */
    @GetMapping("/my")
    public String myBooks(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        List<Book> books = bookService.findMyBooks(loginMember.getId());
        model.addAttribute("books", books);

        return "books/myBookList";
    }

    /**
     * 도서 수정 폼
     */
    @GetMapping("/{bookId}/edit")
    // 수정: name="bookId" 추가
    public String editForm(@PathVariable(name = "bookId") Long bookId, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        Book book = bookService.findOne(bookId);
        if (!book.getSeller().getId().equals(loginMember.getId())) {
            return "redirect:/books/" + bookId;
        }

        BookForm form = new BookForm();
        form.setId(book.getId());
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setPublisher(book.getPublisher());
        form.setPrice(book.getPrice());
        form.setCategory(book.getCategory());
        form.setBookCondition(book.getBookCondition());
        form.setDescription(book.getDescription());
        form.setImageUrl(book.getImageUrl());

        model.addAttribute("bookForm", form);
        model.addAttribute("bookConditions", BookCondition.values());

        return "books/editBookForm";
    }

    /**
     * 도서 수정 처리
     */
    @PostMapping("/{bookId}/edit")
    // 수정: name="bookId" 추가
    public String edit(@PathVariable(name = "bookId") Long bookId, @Valid BookForm form,
                       BindingResult result, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("bookConditions", BookCondition.values());
            return "books/editBookForm";
        }

        try {
            bookService.updateBook(
                    bookId,
                    loginMember.getId(),
                    form.getTitle(),
                    form.getAuthor(),
                    form.getPublisher(),
                    form.getPrice(),
                    form.getCategory(),
                    form.getBookCondition(),
                    form.getDescription(),
                    form.getImageUrl()
            );
        } catch (IllegalStateException e) {
            result.reject("updateFailed", e.getMessage());
            model.addAttribute("bookConditions", BookCondition.values());
            return "books/editBookForm";
        }

        return "redirect:/books/" + bookId;
    }

    /**
     * 도서 삭제
     */
    @PostMapping("/{bookId}/delete")
    // 수정: name="bookId" 추가
    public String delete(@PathVariable(name = "bookId") Long bookId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        try {
            bookService.deleteBook(bookId, loginMember.getId());
        } catch (IllegalStateException e) {
            return "redirect:/books/" + bookId;
        }

        return "redirect:/books/my";
    }
}