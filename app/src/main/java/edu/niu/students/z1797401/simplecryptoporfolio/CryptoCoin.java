/***********************************************************************
 *     Class Name: CryptoCoin.java
 *
 *   Purpose: the class that represents  each cryptoCoin
 *
 ************************************************************************/
package edu.niu.students.z1797401.simplecryptoporfolio;

public class CryptoCoin {
    private int id;                    //The id of the coin in the database
    private String coinID;             //The id of the coin for coinGecko api
    private String name;               //The name of the coin
    private String symbol;             //The symbol of the coin
    private String pictureURL;         //The url for the icon
    private double amount;             //Amount of the coins bought
    private double price;              //the price bought at
    private double totalValue;         //The overall value


    //Constructor for the coin
    public CryptoCoin(int newId,String newCoinId, String newName, String newSymbol, String newPictureURl, double newAmount, double newPrice){
        setID(newId);
        setCoinID(newCoinId);
        setName(newName);
        setSymbol(newSymbol);
        setPictureURL(newPictureURl);
        setAmount(newAmount);
        setPrice(newPrice);
        setTotalValue();
    }


    //Setters
    public void setID(int newId){
        id = newId;
    }
    public void setCoinID(String newCoinID){coinID=newCoinID;}
    public void setName(String newName){
        name = newName;
    }
    public void setSymbol(String newSymbol){symbol = newSymbol;}
    public void setPictureURL(String newPictureURL){pictureURL = newPictureURL;}
    public void setAmount(double newAmount){
        amount = newAmount;
    }
    public void setPrice(double newPrice){
        price = newPrice;
    }
    public void setTotalValue(){
        totalValue = amount*price;
    }

    //Getters
    public int getID(){
        return id;
    }
    public String getCoinID(){return coinID;}
    public String getName(){
        return name;
    }
    public String getSymbol(){return symbol;}
    public String getPictureURL(){return pictureURL;};
    public double getAmount(){
        return amount;
    }
    public double getPrice(){
        return price;
    }
    public double getTotalValue(){
        return totalValue;
    }

}
