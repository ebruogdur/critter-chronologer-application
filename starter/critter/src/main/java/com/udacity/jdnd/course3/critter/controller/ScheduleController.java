package com.udacity.jdnd.course3.critter.controller;


import com.udacity.jdnd.course3.critter.dto.ScheduleDTO;
import com.udacity.jdnd.course3.critter.enums.EmployeeSkill;
import com.udacity.jdnd.course3.critter.model.Customer;
import com.udacity.jdnd.course3.critter.model.Employee;
import com.udacity.jdnd.course3.critter.model.Pet;
import com.udacity.jdnd.course3.critter.model.Schedule;
import com.udacity.jdnd.course3.critter.service.CustomerService;
import com.udacity.jdnd.course3.critter.service.EmployeeService;
import com.udacity.jdnd.course3.critter.service.PetService;
import com.udacity.jdnd.course3.critter.service.ScheduleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles web requests related to Schedules.
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    CustomerService customerService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    PetService petService;
    @Autowired
    ScheduleService scheduleService;
    @Autowired
    ModelMapper modelMapper;


    @PostMapping
    public ScheduleDTO createSchedule(@RequestBody ScheduleDTO scheduleDTO) {

        Schedule schedule = getScheduleFromDTO(scheduleDTO);

        List<Long> employeeIds = scheduleDTO.getEmployeeIds();
        List<Long> petIds = scheduleDTO.getPetIds();

        List<Employee> employees = employeeIds.stream().map(employeeId -> {
            Employee employee = employeeService.getEmployeeById(employeeId);
            return employee;
        }).collect(Collectors.toList());

        List<Pet> pets = petService.getAllPetsByIds(petIds);

        schedule.setEmployees(employees);
        schedule.setPets(pets);


        Schedule savedSchedule = scheduleService.saveSchedule(schedule);

        return getScheduleDTO(savedSchedule);
    }

    @GetMapping
    public List<ScheduleDTO> getAllSchedules() {

        List<Schedule> schedules = scheduleService.getAllSchedules();

        List<ScheduleDTO> scheduleDTOs = new ArrayList<>();
        for(Schedule schedule : schedules){
            scheduleDTOs.add(getScheduleDTO(schedule));
        }

        return scheduleDTOs;
    }

    @GetMapping("/pet/{petId}")
    public List<ScheduleDTO> getScheduleForPet(@PathVariable long petId) {

        List<Schedule> schedules = scheduleService.getSchedulesForPet(petId);

        return schedules.stream().map(this::getScheduleDTO).collect(Collectors.toList());
    }

    @GetMapping("/employee/{employeeId}")
    public List<ScheduleDTO> getScheduleForEmployee(@PathVariable long employeeId) {

        List<Schedule> schedules = scheduleService.getScheduleForEmployee(employeeId);

        return schedules.stream().map(this::getScheduleDTO).collect(Collectors.toList());
    }

    @GetMapping("/customer/{customerId}")
    public List<ScheduleDTO> getScheduleForCustomer(@PathVariable long customerId) {

        List<Schedule> schedules = scheduleService.getScheduleForCustomer(customerId);

        return schedules.stream().map(this::getScheduleDTO).collect(Collectors.toList());
    }

    private ScheduleDTO getScheduleDTO(Schedule schedule) {
        ScheduleDTO scheduleDTO = new ScheduleDTO();

        Set<EmployeeSkill> skills;

        if(schedule.getActivities() != null){
            skills = new HashSet<EmployeeSkill>(schedule.getActivities());
        }else{
            skills = new HashSet<EmployeeSkill>();
        }

        scheduleDTO.setActivities(skills);
        scheduleDTO.setDate(schedule.getDate());
        List<Long> employeeIds = schedule.getEmployees().stream().map(Employee::getId).collect(Collectors.toList());
        scheduleDTO.setEmployeeIds(employeeIds);
        List<Long> petIds = schedule.getPets().stream().map(Pet::getId).collect(Collectors.toList());
        scheduleDTO.setPetIds(petIds);
        scheduleDTO.setId(schedule.getId());

        return scheduleDTO;
    }

    private Schedule getScheduleFromDTO(ScheduleDTO scheduleDTO) {
       Schedule schedule= modelMapper.map(scheduleDTO, Schedule.class);


        /*
        Schedule schedule = new Schedule();

        BeanUtils.copyProperties(scheduleDTO, schedule);
*/
        return schedule;
    }
}