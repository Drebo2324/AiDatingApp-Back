package com.drebo.chatbot_ai_backend;

import com.drebo.chatbot_ai_backend.profiles.Mbt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Utils {

    public static int randomNumber(int min, int max){
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public static Mbt randomMbt(){
        Random random = new Random();

        Mbt[] mbtValues = Mbt.values();

        int i = random.nextInt(mbtValues.length);
        return mbtValues[i];
    }

    public static String randomEthnicity(){

        List<String> ethnicities = new ArrayList<>(List.of(
                "Asian",
                "Hispanic",
                "Black",
                "White",
                "Indian",
                "South East Asian",
                "Middle Eastern",
                "Native American"));

        Collections.shuffle(ethnicities);
        return ethnicities.getFirst();
    }
}

