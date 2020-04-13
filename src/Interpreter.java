public class Interpreter {

    private int AX=0, BX=0, CX=0, DX=0, orderCounter=0;
    public Interpreter() {}

    private void update_proc(PCB proces) {
        proces.AX=this.AX;
        proces.BX=this.BX;
        proces.CX=this.CX;
        proces.DX=this.DX;
        proces.ip=this.orderCounter;
    }

    private void take_from_proc(PCB proces) {
        this.AX=proces.AX;
        this.BX=proces.BX;
        this.CX=proces.CX;
        this.DX=proces.DX;
        this.orderCounter=proces.ip;
    }

    public void execute_instruction(PCB proces) {
        int final_value;
        take_from_proc(proces);

        String order = read_order(proces);
        if(order.equals("CP"))
        {
            String name_of_process = read_operands(proces);
            String source_of_program = read_operands(proces);
            Shell.kontener.create_proc(name_of_process, source_of_program, proces);
            int pid = Shell.kontener.find_proc(name_of_process).PID;
            boolean t = Shell.virtual_memory.load_program(source_of_program, pid);
            if (!t) {
                Shell.kontener.kill_proc(pid);
            }
        }

        if(order.equals("MF"))
        {
            String operand = read_operands(proces);
            String name_of_file = operand.substring(1,operand.length()-1);
            Shell.pliki.createFile(name_of_file);
        }

        if(order.equals("WF"))
        {
            String[] operands = new String[2];
            for(int i=0; i<2; i++)
            {
                operands[i]=read_operands(proces);
            }
            String name_of_file = operands[0].substring(1,operands[0].length()-1);
            String text_to_add = operands[1].substring(1,operands[1].length()-1);
            Shell.pliki.writeDataFile(name_of_file, text_to_add);
        }

        if(order.equals("RF"))
        {
            String[] operands = new String[2];
            operands[0] = read_operands(proces);
            operands[1] = read_operands(proces);
            System.out.println(Shell.pliki.read(operands[0].substring(1,operands[0].length()-1), Integer.parseInt(operands[1])));
        }

        if(order.equals("OF"))
        {
            String operand = read_operands(proces);
            if(Shell.katalog.checkIfExist(operand.substring(1, operand.length()-1)))
            {
                if(Shell.pliki.openFile(operand.substring(1, operand.length()-1), proces))
                    System.out.println("Otwarto plik "+operand.substring(1,operand.length()-1));
            }
        }

        if(order.equals("CF"))
        {
            String operand = read_operands(proces);

            if(Shell.katalog.checkIfExist(operand.substring(1, operand.length()-1)))
            {
                if(Shell.pliki.closeFile(operand.substring(1, operand.length()-1), proces))
                    System.out.println("Zamknieto plik "+operand.substring(1,operand.length()-1));

            }
        }

        if(order.equals("JZ") || order.equals("JP"))
        {
            String operands = read_operands(proces);
            StringBuilder cell_to_jump = new StringBuilder();

            for (int i = 1; i < operands.length() - 1; i++)
                cell_to_jump.append(operands.charAt(i));

            if(order.equals("JZ") && this.CX > 0)
                this.orderCounter = Integer.parseInt(cell_to_jump.toString());
            if(order.equals("JP"))
                this.orderCounter = Integer.parseInt(cell_to_jump.toString());

        }

        if(order.equals("IC") || order.equals("DC"))
        {
            String operands = read_operands(proces);
            switch (operands)
            {
                case "AX":
                    if(order.equals("IC"))
                        this.AX++;
                    else
                        this.AX--;
                    break;
                case "BX":
                    if(order.equals("IC"))
                        this.BX++;
                    else
                        this.BX--;
                    break;
                case "CX":
                    if(order.equals("IC"))
                        this.CX++;
                    else
                        this.CX--;
                    break;
                case "DX":
                    if(order.equals("IC"))
                        this.DX++;
                    else
                        this.DX--;
                    break;
            }
        }

        if(order.equals("AD") || order.equals("SB") || order.equals("ML") || order.equals("DV") || order.equals("MV")) {
            final_value = 0;
            String[] operands = new String[2];
            for(int i=0; i<2; i++)
            {
                operands[i]=read_operands(proces);
            }

            //---------------KONWERSJA ODCZYTANEJ LICZBY ZE STRINGA NA INT--------------------------------------------
            //---------------WYKRYWANIE CZY LICZBA JEST POODANA BEZPOSREDNIO CZY TEZ POBIERANA Z PAMIECI--------------
            if (operands[1].charAt(0) == '[') {
                StringBuilder cell_as_string = new StringBuilder();

                for (int i = 1; i < operands[1].length() - 1; i++) {
                    cell_as_string.append(operands[1].charAt(i));
                }

                int number_of_cell = Integer.parseInt(cell_as_string.toString());
                final_value = (int) Shell.memory.load_data(number_of_cell, proces.PID);

            }
            else if (operands[1].equals("AX") || operands[1].equals("BX") || operands[1].equals("CX") || operands[1].equals("DX")) {
                switch (operands[1]) {
                    case "AX":
                        final_value = this.AX;
                        break;
                    case "BX":
                        final_value = this.BX;
                        break;
                    case "CX":
                        final_value = this.CX;
                        break;
                    case "DX":
                        final_value = this.DX;
                        break;
                }
            } else {
                final_value = Integer.parseInt(operands[1]);
            }

            switch (operands[0]) {
                case "AX":
                    switch (order) {
                        case "AD":
                            this.AX = this.AX + final_value;
                            break;
                        case "SB":
                            this.AX = this.AX - final_value;
                            break;
                        case "ML":
                            this.AX = this.AX * final_value;
                            break;
                        case "DV":
                            this.AX = this.AX / final_value;
                            break;
                        case "MV":
                            this.AX = final_value;
                            break;
                    }
                    break;
                case "BX":
                    switch (order) {
                        case "AD":
                            this.BX = this.BX + final_value;
                            break;
                        case "SB":
                            this.BX = this.BX - final_value;
                            break;
                        case "ML":
                            this.BX = this.BX * final_value;
                            break;
                        case "DV":
                            this.BX = this.BX / final_value;
                            break;
                        case "MV":
                            this.BX = final_value;
                            break;
                    }
                    break;
                case "CX":
                    switch (order) {
                        case "AD":
                            this.CX = this.CX + final_value;
                            break;
                        case "SB":
                            this.CX = this.CX - final_value;
                            break;
                        case "ML":
                            this.CX = this.CX * final_value;
                            break;
                        case "DV":
                            this.CX = this.CX / final_value;
                            break;
                        case "MV":
                            this.CX = final_value;
                            break;
                    }
                    break;
                case "DX":
                    switch (order) {
                        case "AD":
                            this.DX = this.DX + final_value;
                            break;
                        case "SB":
                            this.DX = this.DX - final_value;
                            break;
                        case "ML":
                            this.DX = this.DX * final_value;
                            break;
                        case "DV":
                            this.DX = this.DX / final_value;
                            break;
                        case "MV":
                            this.DX = final_value;
                            break;
                    }
                    break;
            }
        }
        if(!order.equals("HT")) {

            update_proc(proces);
            proces.display_all();

        }
        else {
            proces.display_all();
            Shell.kontener.kill_proc(proces.PID);
        }
    }

    private String read_order(PCB proces) {
        this.orderCounter=proces.ip;
        while(Shell.memory.load_data(this.orderCounter, proces.PID)==' ') {
            this.orderCounter++;
        }
        StringBuilder order = new StringBuilder();
        for(int i=0; i<2; i++)
        {
            order.append(Shell.memory.load_data(this.orderCounter, proces.PID));
            this.orderCounter++;
        }
        return order.toString();
    }

    private String read_operands(PCB proces) {
        StringBuilder operand = new StringBuilder();
        if(Shell.memory.load_data(this.orderCounter, proces.PID)==' ')
            this.orderCounter++;

        while(Shell.memory.load_data(this.orderCounter, proces.PID)!=' ')
        {
            operand.append(Shell.memory.load_data(this.orderCounter, proces.PID));
            this.orderCounter++;
        }

        return operand.toString();
    }
}
