package com.example.raid_bot_2;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class User {

    @JsonProperty("user_ids")
    List<Long> userId = new ArrayList<>();

}
