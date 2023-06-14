package dtos;

import entities.Festival;
import entities.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FestivalDTO {
    private Long id;
    private String name;
    private String city;
    private String startDate;
    private String endDate;
    private List<UserDTO> guests = new ArrayList<>();
    private List<ShowDTO> shows = new ArrayList<>();

    public FestivalDTO(String name, String city, String startDate, String endDate) {
        this.name = name;
        this.city = city;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public FestivalDTO(Festival festival) {
        this.id = festival.getId();
        this.name = festival.getName();
        this.city = festival.getCity();
        this.startDate = festival.getStartDate().toString();
        this.endDate = festival.getEndDate().toString();
        if (festival.getGuests() != null) {
            this.guests = UserDTO.getUserDTOs(festival.getGuests());
        }
        if (festival.getShows() != null) {
            this.shows = ShowDTO.getShowDTOs(festival.getShows());
        }
    }

    public static List<FestivalDTO> getFestivalDTOs(List<Festival> festivals){
        List<FestivalDTO> festivalDTOs = new ArrayList<>();
        festivals.forEach(festival -> festivalDTOs.add(new FestivalDTO(festival)));
        return festivalDTOs;
    }
}
