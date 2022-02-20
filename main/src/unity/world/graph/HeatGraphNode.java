package unity.world.graph;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.ui.*;

import static unity.graphics.UnityPal.*;

public class HeatGraphNode extends GraphNode<HeatGraph>{
    public static final float celsiusZero = 273.15f;
    public static float ambientTemp = celsiusZero + 20;
    public float flux = 0;
    public float heatenergy = 1f;
    float energyBuffer = 0; //write to
    public float emissiveness = 0.01f;
    public float conductivity = 0.1f;
    public float heatcapacity = 1f;
    public float maxTemp = celsiusZero +1000;

    public boolean heatProducer = false;
    public float targetTemp = 1000; public float prodEfficency = 0.1f;
    public float efficency = 0;

    public HeatGraphNode(GraphBuild build, float emissiveness, float conductivity, float heatcapacity, float maxTemp){
        super(build);
        this.emissiveness = emissiveness;
        this.conductivity = conductivity;
        this.maxTemp = maxTemp;
        this.heatcapacity = heatcapacity;
        energyBuffer = this.heatenergy = heatcapacity * ambientTemp;
    }
    public HeatGraphNode(GraphBuild build, float emissiveness, float conductivity, float heatcapacity, float maxTemp, float targetTemp , float prodEfficency){
       super(build);
       this.emissiveness = emissiveness;
       this.conductivity = conductivity;
       this.maxTemp = maxTemp;
       this.targetTemp = targetTemp;
       this.prodEfficency=prodEfficency;
       this.heatProducer = true;
       this.heatcapacity = heatcapacity;
       energyBuffer = this.heatenergy = heatcapacity * ambientTemp;
   }

    public HeatGraphNode(GraphBuild build){
        super(build);
    }

    @Override
    public void displayBars(Table table){
        table.row();
        table.add(new Bar(() -> Core.bundle.format("bar.unity-temp", Strings.fixed(getTemp() - celsiusZero, 1)), this::heatColor, () -> Mathf.clamp(Math.abs(getTemp() / 273))));
    }

    @Override
    public void update(){
        //graph handles all heat transmission.
        heatenergy += (ambientTemp - getTemp()) * emissiveness * Time.delta / 60f;
        if(heatProducer){
            generateHeat();
        }
    }

    public Color heatColor(){
        Color c = new Color();
        heatColor(c);
        return c;
    }

    public void heatColor(Color input){
        heatColor(getTemp(),input);
    }
    public static void heatColor(float t,Color input){
        float a = 0;
        if(t > celsiusZero){
            a = Math.max(0, (t - 498) * 0.001f);
            if(a < 0.01){
                input.set(Color.clear);
                return;
            }
            input.set(heatcolor.r, heatcolor.g, heatcolor.b, a);
            if(a > 1){
                input.add(0, 0, 0.01f * a);
                input.mul(a);
            }
        }else{
            a = 1.0f - Mathf.clamp(t / celsiusZero);
            if(a < 0.01){
                input.set(Color.clear);
            }
            input.set(coldcolor.r, coldcolor.g, coldcolor.b, a);
        }
    }

    @Override
    public void read(Reads read){
        this.energyBuffer = this.heatenergy = read.f();
    }

    @Override
    public void write(Writes write){
        write.f(this.heatenergy );
    }

    public void generateHeat(float targetTemp, float eff){
        heatenergy += (targetTemp-getTemp())*eff;
    }
    public void generateHeat(){
        heatenergy += (targetTemp-getTemp())*efficency*prodEfficency;
    }

    public float getTemp(){
        return heatenergy/ heatcapacity;
    }

    public void setTemp(float temp){
        heatenergy = temp*heatcapacity;
    }

    public void addHeatEnergy(float e){
            heatenergy += e;
        }
}
