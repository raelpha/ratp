package voyageur;

public class VoyageurConstants
{
    public static int coutChgtStation = 5;
    public static float Discretisation = 0.1f;

    // Dans Station
    public static float voyageurScale = 2.5f;
    public static float maximumDistanceStation = 15; // peut être adapter pour chaque station
    public static float probabiliteDeBouger = 0.0005f;
    public static float vitesse = 0.05f;
    public static float probabiliteIncidentVoyageur = 0.001f;

    // Colere
    public static int colereMax = 100;
    public static int augmentationColereStationFermee = 40;
    public static int augmentationColereParStationSupplementaire = 5;
    public static int augmentationColereParNvChgtLigne = 25;
    public static int distanceInfluence = 3;
    public static double vitesseDeColerisation = 0.00000001;
    public static int updateColere = 500;
    public static int colereMoyenneDeDepart = 0;
}
