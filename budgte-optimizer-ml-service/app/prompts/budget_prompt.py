class BudgetPrompt:
    @staticmethod
    def build_analysis_prompt(data: dict) -> str:
        return "Analiza estos datos de presupuesto y sugiere mejoras." \
            f" Datos: {data}"

    @staticmethod
    def build_prediction_prompt(data: dict) -> str:
        return "Predice el presupuesto para el siguiente periodo." \
            f" Datos: {data}"

    @staticmethod
    def build_optimization_prompt(data: dict) -> str:
        return "Optimiza el presupuesto actual con base en los datos proporcionados." \
            f" Datos: {data}"

    @staticmethod
    def build_anomaly_prompt(data: dict) -> str:
        return "Detecta anomalías en los registros de gastos." \
            f" Datos: {data}"
