package DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class Semester {
    int semesterIndex;
    List<Discipline> disciplines;
    LocalDate startDate;
    LocalDate finishDate;
}
