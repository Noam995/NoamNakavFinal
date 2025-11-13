package com.example.noamnakavfinal.model;

public class Sale {
    protected String saleId;

    protected Car car;

    protected User user;

    protected String date;

    protected double price;


    public Sale(String saleId, Car car, User user, String date, double price) {
        this.saleId = saleId;
        this.car = car;
        this.user = user;
        this.date = date;
        this.price = price;
    }


    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Sale() {
    }
    @Override
    public String toString() {
        return "Sale{" +
                "saleId=" + saleId +
                ", car=" + car +
                ", user=" + user +
                ", date='" + date + '\'' +
                ", price=" + price +
                '}';
    }
}
