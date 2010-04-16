REM Chemkin-Generated Batch Job Command File

REM get the Chemkin runtime environment defined

CALL "C:\Program Files\Reaction\chemkin15082_pc\bin\run_chemkin_env_setup.bat"
SET JAVA_ID=218715227
SET FLEXLM_DIAGNOSTICS=3
SET

REM DELETE ANY INTERMEDIATE FILES LEFTOVER FROM PREVIOUS RUN

IF EXIST "C:\Documents and Settings\nmvdewie\My Documents\chemkin\Parameter_Estimation_v2_multithreading\XMLdata.zip" DEL /F /Q "C:\Documents and Settings\nmvdewie\My Documents\chemkin\Parameter_Estimation_v2_multithreading\XMLdata.zip"

REM COPY FILES TO WORKING DIR

COPY "C:\Program Files\Reaction\chemkin15082_pc\data\chemkindata.dtd" "C:\Documents and Settings\nmvdewie\My Documents\chemkin\Parameter_Estimation_v2_multithreading"

REM RUN THE JOB AND CAPTURE EXIT STATUS

CKReactorPlugFlow -i "C:\Documents and Settings\nmvdewie\My Documents\chemkin\Parameter_Estimation_v2_multithreading\test-kevin-pro.inp" -o "C:\Documents and Settings\nmvdewie\My Documents\chemkin\Parameter_Estimation_v2_multithreading\test-kevin-pro.out" -m "test-kevin-pro.out.mon" -c "C:\Documents and Settings\nmvdewie\My Documents\chemkin\Parameter_Estimation_v2_multithreading\test-kevin-pro_gas.asc"
exit %errorlevel%
