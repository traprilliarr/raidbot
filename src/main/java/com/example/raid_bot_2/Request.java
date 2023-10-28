package com.example.raid_bot_2;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Request {

    @Id
    private String id;

    @Column(name = "twitterLink")
    private String twitterLink;

    @Column(name = "fromId")
    private Long fromId;

    @Column(name = "idMessage")
    private Integer idMessage;
    @Column(name = "firstName")
    private String firstName;
    @Column(name = "likes")
    private Integer likes;
    @Column(name = "reposts")
    private Integer repost;
    @Column(name = "replies")
    private Integer replies;
    @Column(name = "bookmarks")
    private Integer bookmarks;
    @Column(name = "dateTime")
    private LocalDateTime dateTime;
}
