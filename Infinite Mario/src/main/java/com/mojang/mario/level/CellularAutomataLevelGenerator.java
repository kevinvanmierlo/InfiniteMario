/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mojang.mario.level;

/**
 *
 * @author kevinvanmierlo
 */
public class CellularAutomataLevelGenerator 
{
    private int[][] land;
    private int width;
    private int height;
    
    public CellularAutomataLevelGenerator(int width, int height)
    {
        land = new int[width][height];
        this.width = width;
        this.height = height;
        generateLand();
        processN(10);
    }
    
    public int[][] getLand()
    {
        return land;
    }
    
    public void processN(int n) 
    {
        //*
        for(int i = 0;i < n; i++){
                //land = process(land);
            //generateLand();    
            land = updateLand(land);
        }
        //*/
    }
    
    private int[][] nextToLand(int[][] land, int tile){
        int[][] temp = new int[width][height];
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                temp[i][j] = 0;
                if(j>0){
                    temp[i][j] += (land[i][j-1] == tile) ? 1 : 0; // left
                                    }
                if(j < land[0].length - 1){
                    temp[i][j] += (land[i][j+1] == tile) ? 1 : 0; // right
                }
            }
        }
        return temp;
    }
    
    private int[][] betweenLand(int[][] land, int tile){
        int[][] temp = new int[width][height];
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                temp[i][j] = 0;
                if(i>0){
                    temp[i][j] += (land[i-1][j] == tile) ? 1 : 0; // top
                }
                if(i < land.length - 1){
                    temp[i][j] += (land[i+1][j] == tile) ? 1 : 0; // bottom
                }
            }
        }
        return temp;
    }
    
    private int[][] neighboorsLand(int[][] land){
        int[][] temp = new int[width][height];
        for(int i = 0; i < land.length; i++){
            for(int j = 0; j < land[0].length; j++){
                temp[i][j] = 0;
                if(j>0){
                    temp[i][j] += land[i][j-1]; // left
                    if(i>0){
                        temp[i][j] += land[i-1][j-1]; // top-left
                    }
                    if(i<land.length - 1){
                        temp[i][j] += land[i+1][j-1]; // bottom-left
                    }
                }
                if(j < land[0].length - 1){
                    temp[i][j] += land[i][j+1]; // right
                    if(i>0){
                        temp[i][j] += land[i-1][j+1]; // top-right
                    }
                    if(i<land.length - 1){
                        temp[i][j] += land[i+1][j+1]; // bottom-right
                    }
                }
                if(i>0){
                    temp[i][j] += land[i-1][j]; // top
                }
                if(i<land.length - 1){
                    temp[i][j] += land[i+1][j]; // bottom
                }
            }
        }
        return temp;
    }
    
    private int[][] updateLand(int[][] land){
        //int[][] neighboors = neighboorsLand(land);
        int[][] neighboors = nextToLand(land, 1);
        int[][] topBottom = betweenLand(land, 1);
        int[][] temp = land;
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                //rule one (no neighboors become air)
                if(neighboors[i][j] == 0){
                    temp[i][j] = 0;
                } else if(neighboors[i][j] == 2){ //rule two (block needs to be at least 3)
                        temp[i][j] = 1;
                } else {
                    //*
                    if(land[i][j] == 1){
                        if(neighboors[i][j] == 1){
                            if(j > 0 && neighboors[i][j-1] == 2){
                                temp[i][j] = 1;
                            } else if(j < land[0].length-1 && neighboors[i][j+1] == 2){
                                temp[i][j] = 1;
                            } else {
                                temp[i][j] = 0;
                            }
                        }
                    }
                    //*/
                }
                
                // rule tree (when a bock has no block above and below look at block above it that one has two topbottom neighboors.
                // if it has remove the origional block)
                if(land[i][j] == 1 && topBottom[i][j] == 0){
                    if(i > 0 && topBottom[i-1][j] == 2){
                         temp[i][j] = 0;
                    }
                }
                
                // rule five (if air has 1 neighboor below with a solid neighboor then remove this first neighboor below the air)
                if(land[i][j] == 0 && topBottom[i][j] == 1){
                    if(i > land.length-1 && land[i+1][j] == 1 && topBottom[i+1][j] == 1){
                        temp[i+1][j] = 0;
                    }
                }
            }
        }
        return temp;
        
    }
    
    private int[][] landCleanUp(){
        int[][] temp = land;
        int[][] topBottom = betweenLand(land,1);
        int[][] neighboors = nextToLand(land,1);
        
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                //*
                // rule foor (when air is enclosed in blocks fill the air)
                if(land[i][j] == 0 && topBottom[i][j] == 2 ){//&& ((i > 0 && topBottom[i-1][j] == 1) || (i < (land.length - 2) && topBottom[i+1][j] == 1))){
                    temp[i-1][j] = 1;
                    temp[i][j] = 1;
                    temp[i+1][j] = 1;
                }
                //*/
                
                //*
                if(land[i][j] == 1 && neighboors[i][j] == 0){
                    temp[i][j] = 0;
                }
                //*/
            }
        }
        
        return temp;
    }
    
    private void generateLand(){
        if(land != null){
            for(int i = 0; i < land.length; i++){
                for(int j = 0; j < land[0].length; j++){
                    land[i][j] = ((Math.random() + ((double)i/(land.length*1.7))) > 0.95 ? 1 : 0);
                }
            }
        }
    }
}
