package com.example.telegrambot.dto;

import com.opencsv.bean.CsvBindByName;

public class VacancyDto {
    @CsvBindByName(column = "id")
    private  String id;
    @CsvBindByName(column = "title")
    private  String title;
    @CsvBindByName(column = "Sort description")
    private  String shortDesc;
    @CsvBindByName(column = "Long description")
    private  String longDesc;
    @CsvBindByName(column = "Company")
    private  String company;
    @CsvBindByName(column = "Salary")
    private  String salary;
    @CsvBindByName(column = "Link")
    private  String link;

    public String getLongDesc() {
        return longDesc;
    }

    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }
}
