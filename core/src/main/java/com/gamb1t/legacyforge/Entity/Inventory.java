package com.gamb1t.legacyforge.Entity;

import com.gamb1t.legacyforge.Weapons.Armor;
import com.gamb1t.legacyforge.Weapons.Weapon;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private ArrayList<Weapon> weapons;
    private ArrayList<Armor> armors;


    public Inventory(){
        weapons = new ArrayList<>();
        armors = new ArrayList<>();
    }
    public void addWeapon(Weapon weapon) {
        weapons.add(weapon);
    }

    public void addArmor(Armor armor) {
        armors.add(armor);
    }

    public boolean containsWeaponByName(String name){
        for(Weapon w : weapons){
            if(w.getName().equals(name)){
                return true;
            }
        }
        return false;
    }


    public boolean contains(Weapon weapon){
        return weapons.contains(weapon);
    }

    public Weapon getWeaponByName(String name){
        for(Weapon weapon : weapons){
            if(weapon.getName().equals(name)){
            return  weapon;}
        }
        return null;
    }
    public boolean containsArmorByName(String s){
        for(Armor armor : armors){
            if(armor.getName().equals(s)){
                return true;
            }
        }
        return false;
    }


    public Armor getArmorByName(String name){
        for(Armor armor : armors){
            if(armor.getName().equals(name)){
                return armor;}
        }
        return null;
    }
    public ArrayList<Weapon> getWeapons(){
        return  weapons;
    }

    public ArrayList<Armor> getArmors(){
        return armors;
    }

    public void addWeaponList(List<Weapon> weaponList){
        weapons.addAll(weaponList);
    }
    public void addArmorList(List<Armor> armorsList){
        armors.addAll(armorsList);
    }
 }
