package com.mojang.mario.level;

import java.util.Random;

import com.mojang.mario.sprites.Enemy;


public class LevelGenerator
{
    public static final int TYPE_OVERGROUND = 0;
    public static final int TYPE_UNDERGROUND = 1;
    public static final int TYPE_CASTLE = 2;

    @SuppressWarnings("unused")
	private static Random levelSeedRandom = new Random();
    public static long lastSeed;

    public static Level createLevel(int width, int height, long seed, int difficulty, int type)
    {
        LevelGenerator levelGenerator = new LevelGenerator(width, height);
        return levelGenerator.createLevel(seed, difficulty, type);
    }

    private int width;
    private int height;
    Level level = new Level(width, height);
    Random random;

    private int difficulty;
    private int type;

    private LevelGenerator(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    private Level createLevel(long seed, int difficulty, int type)
    {
        this.type = type;
        this.difficulty = difficulty;

        lastSeed = seed;
        level = new Level(width, height);
        random = new Random(seed);

        CellularAutomataLevelGenerator generator = new CellularAutomataLevelGenerator(width, height);
        int[][] generatedLevel = generator.getLand();
        
        level.xExit = width - 10;
        level.yExit = generator.beginFloorHeight;
        
        type = LevelGenerator.TYPE_OVERGROUND;
        
        int blockShift = 0;
        switch(type){
            case LevelGenerator.TYPE_OVERGROUND:
                blockShift = 0;
            break;
            case LevelGenerator.TYPE_CASTLE:
                blockShift = 4;
            break;
            case LevelGenerator.TYPE_UNDERGROUND:
                blockShift = 8;
            break;
        }
        
        for (int x = 0; x < level.width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                switch(generatedLevel[x][y]){
                    case 0: // air (0 + 0 * 16)
                        level.setBlock(x, y, (byte)(0));
                    break;
                    case 1: // floor (blockShift + 5 + 8 * 16)
                        level.setBlock(x, y, (byte)(blockShift + 5 + 8 * 16));
                        // spawn random mobs in level
                        if(x > 20 && x < width -20 && random.nextDouble() < (0.03*difficulty + (x / (width * 3.00f)))){
                            level.setSpriteTemplate(x, y-1, new SpriteTemplate(random.nextInt(3), random.nextInt(35) < difficulty));
                        }
                    break;
                    case 2: // coin (0 + 2 * 16)
                        level.setBlock(x, y, (byte)(0 + 2 * 16));
                    break;
                    case 3: // wall (blockShift + 5 + 9 * 16)
                        level.setBlock(x, y, (byte)(blockShift + 5 + 9 * 16));
                    break;
                    case 4: // "?" Block (0 + random.nextInt(3) + 1 * 16)
                        level.setBlock(x, y, (byte)(4 + random.nextInt(3) + 1 * 16));
                    break;
                    case 5: // gold Block (0 + random.nextInt(3) + 1 * 16)
                        level.setBlock(x, y, (byte)(0 + random.nextInt(3) + 1 * 16));
                    break;
                    case 10: // left floor edge
                        level.setBlock(x, y, (byte)(blockShift + 0 + 8 * 16));
                    break;
                    case 11: // right floor edge
                        level.setBlock(x, y, (byte)(blockShift + 2 + 8 * 16));
                    break;
                    case 12: // left mountain edge
                        level.setBlock(x, y, (byte)(blockShift + 4 + 8 * 16));
                    break;
                    case 13: // right mountain edge
                        level.setBlock(x, y, (byte)(blockShift + 6 + 8 * 16));
                    break;
                    case 14: // left floor wall
                        level.setBlock(x, y, (byte)(blockShift + 0 + 9 * 16));
                    break;
                    case 15: // right floor wall
                        level.setBlock(x, y, (byte)(blockShift + 2 + 9 * 16));
                    break;
                    case 16: // left mountain wall
                        level.setBlock(x, y, (byte)(blockShift + 4 + 9 * 16));
                    break;
                    case 17:// right mountain wall
                        level.setBlock(x, y, (byte)(blockShift + 6 + 9 * 16));
                    break;
                }
            }
        }

        return level;
    }

    private void addEnemyLine(int x0, int x1, int y)
    {
        for (int x = x0; x < x1; x++)
        {
            if (random.nextInt(35) < difficulty + 1)
            {
                int type = random.nextInt(4);
                if (difficulty < 1)
                {
                    type = Enemy.ENEMY_GOOMBA;
                }
                else if (difficulty < 3)
                {
                    type = random.nextInt(3);
                }
                level.setSpriteTemplate(x, y, new SpriteTemplate(type, random.nextInt(35) < difficulty));
            }
        }
    }
}