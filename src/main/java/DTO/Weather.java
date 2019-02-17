package DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Weather {
    private String name;
    private Double temp;
    private Double humidity;
    private String icon;
    private String main;
}
