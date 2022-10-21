package tech.yakov.AtemProxy.models;

public class Signal {
    private int id;
    private String name;
    private TallyState tallyState;

    public Signal(int id, String name){
        this.id = id;
        this.name = name;
        this.tallyState = TallyState.EMPTY;
    }

    public void setTallyState(TallyState tallyState) {
        this.tallyState = tallyState;
    }

    public String getName(){
        return name;
    }

    public TallyState getTallyState(){
        return this.tallyState;
    }

    public enum TallyState {
        EMPTY((byte) 0),
        PGM((byte) 1),
        PRV((byte) 2),
        PGM_PRV((byte) 3);

        private final byte id;

        TallyState(byte id) {
            this.id = id;
        }

        public static TallyState getTallyState(byte tallyId){
            switch (tallyId) {
                case 1:
                    return TallyState.PGM;
                case 2:
                    return TallyState.PRV;
                case 3:
                    return TallyState.PGM_PRV;
                default:
                    return TallyState.EMPTY;
            }
        }

        public byte getId(){
            return id;
        }

    }
}
