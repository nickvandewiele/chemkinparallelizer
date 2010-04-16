REM Chemkin-Generated Batch Job Command File

REM get the Chemkin runtime environment defined

CALL "C:\Program Files\Reaction\chemkin15082_pc\bin\run_chemkin_env_setup.bat"
SET JAVA_ID=218715227
SET FLEXLM_DIAGNOSTICS=3
SET

REM RUN THE JOB AND CAPTURE EXIT STATUS

CKPreProcess.EXE -i "C:\Documents and Settings\nmvdewie\My Documents\chemkin\Parameter_Estimation_v2_multithreading\CKPreProc_test-kevin-pro.input"
exit %errorlevel%
