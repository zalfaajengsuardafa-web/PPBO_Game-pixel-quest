package game.entities;

public class CharacterStats {
    private int level    = 1;
    private int exp      = 0;
    private int expToNext = 100;
    private int attackPower = 12;

    public boolean addExp(int amount) {
        exp += amount;
        if (exp >= expToNext) {
            exp -= expToNext;
            level++;
            attackPower += 3;
            expToNext   = (int)(expToNext * 1.5f);
            return true;
        }
        return false;
    }

    public int getLevel()       { return level; }
    public int getExp()         { return exp; }
    public int getExpToNext()   { return expToNext; }
    public int getAttackPower() { return attackPower; }
}