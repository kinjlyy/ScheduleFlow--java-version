package com.scheduleflow.resource.controller;

import com.scheduleflow.resource.dto.RoomDTO;
import com.scheduleflow.resource.model.RoomType;
import com.scheduleflow.resource.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
@AutoConfigureMockMvc
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @Test
    void getAllRooms_shouldReturnRoomList() throws Exception {
        RoomDTO room = new RoomDTO(1L, "R101", 50, RoomType.CLASSROOM, true, true, false, true);
        given(roomService.getAllRooms()).willReturn(List.of(room));

        mockMvc.perform(get("/api/rooms").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomNumber").value("R101"))
                .andExpect(jsonPath("$[0].maximumCapacity").value(50));
    }
}
