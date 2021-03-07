package fyp.gy.main_server.controller;

import fyp.gy.main_server.service.MachineService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MachineController {

    private final MachineService machineService;


    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }


    @PostMapping("/machine/register")
    public void registerMachine(@RequestParam String machineID) {

        try {
            machineService.registerMachine(machineID);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e
            );
        }

    }


}
