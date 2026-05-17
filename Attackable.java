package game.entities;

public interface Attackable {
    int attack();
    int attack(int bonusDamage);
    int attack(String skillName);
    boolean gainExp(int amount);
}