package com.example.noamnakavfinal.model;

public class Car {
    protected String id;
    protected String licenseCar;
    protected String brand;
    protected String model;
    protected String color;
    protected String year;
    protected double price;
    protected double km;
    protected int hand;
    protected String gearbox;
    protected String ownership;
    protected String gas;
    protected String timeUntilTest;
    protected double engineVolume;
    protected String engine;
    protected boolean available;

    public Car(String id, String licenseCar, String brand, String model, String color, String year, double price, double km, int hand, String gearbox, String ownership, String gas, String timeUntilTest, double engineVolume, String engine, boolean available) {
        this.id = id;
        this.licenseCar = licenseCar;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.year = year;
        this.price = price;
        this.km = km;
        this.hand = hand;
        this.gearbox = gearbox;
        this.ownership = ownership;
        this.gas = gas;
        this.timeUntilTest = timeUntilTest;
        this.engineVolume = engineVolume;
        this.engine = engine;
        this.available = available;
    }

    public Car() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLicenseCar() {
        return licenseCar;
    }

    public void setLicenseCar(String licenseCar) {
        this.licenseCar = licenseCar;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }

    public int getHand() {
        return hand;
    }

    public void setHand(int hand) {
        this.hand = hand;
    }

    public String getGearbox() {
        return gearbox;
    }

    public void setGearbox(String gearbox) {
        this.gearbox = gearbox;
    }

    public String getOwnership() {
        return ownership;
    }

    public void setOwnership(String ownership) {
        this.ownership = ownership;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getTimeUntilTest() {
        return timeUntilTest;
    }

    public void setTimeUntilTest(String timeUntilTest) {
        this.timeUntilTest = timeUntilTest;
    }

    public double getEngineVolume() {
        return engineVolume;
    }

    public void setEngineVolume(double engineVolume) {
        this.engineVolume = engineVolume;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Car{" +
                "id='" + id + '\'' +
                ", licenseCar='" + licenseCar + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", color='" + color + '\'' +
                ", year='" + year + '\'' +
                ", price=" + price +
                ", km=" + km +
                ", hand=" + hand +
                ", gearbox='" + gearbox + '\'' +
                ", ownership='" + ownership + '\'' +
                ", gas='" + gas + '\'' +
                ", timeUntilTest='" + timeUntilTest + '\'' +
                ", engineVolume=" + engineVolume +
                ", engine='" + engine + '\'' +
                ", available=" + available +
                '}';
    }
}


