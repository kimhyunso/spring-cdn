package com.project.hyunso.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.hyunso.domain.Article;
import com.project.hyunso.domain.User;
import com.project.hyunso.domain.UserRole;
import com.project.hyunso.dto.AddArticleRequest;
import com.project.hyunso.dto.UpdateArticleRequest;
import com.project.hyunso.repository.BlogRepository;
import com.project.hyunso.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BlogApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    User user;


    @Autowired
    private BlogRepository blogRepository;

    @BeforeEach
    public void setSecurityContext(){
        userRepository.deleteAll();
        user = userRepository.save(User.builder()
                .email("user@gmail.com")
                .password("test")
                .userRole(UserRole.ROLE_USER)
                .build());
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    }


    @BeforeEach
    public void mockMvcSetUp(){

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        blogRepository.deleteAll();
    }

    @DisplayName("addArticle: 블로그 글 추가에 성공한다.")
    @Test
    public void addArticle() throws Exception{
        // given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        // 객체 JSON으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(userRequest);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        // when
        // 설정한 내용을 바탕으로 요청 전송

        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal)
                .content(requestBody));


        // then
        result.andExpect(status().isCreated());

        List<Article> articles = blogRepository.findAll();

        assertThat(articles.size()).isEqualTo(1); // 크기가 1인지 검증
        assertThat(articles.get(0).getTitle()).isEqualTo(title);
        assertThat(articles.get(0).getContent()).isEqualTo(content);
    }


    @DisplayName("findAllArticles: 블로그 글 목록 조회에 성공한다.")
    @Test
    public void findAllArticles() throws Exception{
        // given
        final String url = "/api/articles";
        Article savedArticle = createDefaultArticle();

        // when
        blogRepository.save(Article.builder()
                        .title(savedArticle.getTitle())
                        .content(savedArticle.getContent())
                        .author(savedArticle.getAuthor())
                        .build());

        // then
        final ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON));

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value(savedArticle.getContent()))
                .andExpect(jsonPath("$[0].title").value(savedArticle.getTitle()));
    }

    @DisplayName("findByIdArticle: 블로그 글 조회에 성공한다.")
    @Test
    public void findArticle() throws Exception{
        // given
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        // when
        Article saveArticle = blogRepository.save(Article.builder()
                        .content(savedArticle.getContent())
                        .title(savedArticle.getTitle())
                        .author(savedArticle.getAuthor())
                        .build());
        ResultActions result = mockMvc.perform(get(url, saveArticle.getId()));

        // then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(savedArticle.getContent()))
                .andExpect(jsonPath("$.title").value(savedArticle.getTitle()));
    }

    @DisplayName("deleteArticle: 블로그 글 삭제에 성공한다.")
    @Test
    public void deleteArticle() throws Exception{
        // given
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        // when 
//        Article saveArticle = blogRepository.save(Article.builder()
//                        .title(savedArticle.getTitle())
//                        .content(savedArticle.getContent())
//                        .author(savedArticle.getAuthor())
//                        .build());

        // void라 return 결과가 없다. 그렇기 때문에 ResultActions를 작성안함
        mockMvc.perform(delete(url, savedArticle.getId()));

        // then
        List<Article> articles = blogRepository.findAll();

        assertThat(articles).isEmpty();
    }

    @DisplayName("updateArticle: 블로그 글 수정에 성공한다.")
    @Test
    public void updateArticle() throws Exception{
        // given
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        Article saveArticle = blogRepository.save(Article.builder()
                        .content(savedArticle.getContent())
                        .title(savedArticle.getTitle())
                        .author(savedArticle.getAuthor())
                        .build());

        final String newTitle = "새로운 제목";
        final String newContent = "새로운 내용";
        UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newContent);

        // when
        ResultActions result = mockMvc.perform(put(url, saveArticle.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk());

        Article article = blogRepository.findById(saveArticle.getId()).get();

        assertThat(article.getTitle()).isEqualTo(newTitle);
        assertThat(article.getContent()).isEqualTo(newContent);
    }


    private Article  createDefaultArticle(){
        return blogRepository.save(Article.builder()
                        .title("title")
                        .author(user.getUsername())
                        .content("content")
                        .build());
    }
}